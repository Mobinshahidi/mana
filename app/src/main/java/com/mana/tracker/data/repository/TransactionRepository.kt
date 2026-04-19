package com.mana.tracker.data.repository

import android.content.ContentResolver
import android.provider.Telephony
import com.mana.tracker.data.local.dao.AccountDao
import com.mana.tracker.data.local.dao.ParserStateDao
import com.mana.tracker.data.local.dao.TransactionDao
import com.mana.tracker.data.local.entity.AccountEntity
import com.mana.tracker.data.local.entity.ParserStateEntity
import com.mana.tracker.data.local.entity.TransactionEntity
import com.mana.tracker.data.parser.BankSmsParser
import com.mana.tracker.data.parser.ParsedTransaction
import com.mana.tracker.data.parser.ParserUtils
import com.mana.tracker.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val contentResolver: ContentResolver,
    private val transactionDao: TransactionDao,
    private val accountDao: AccountDao,
    private val parserStateDao: ParserStateDao,
    private val bankParsers: List<BankSmsParser>
) {

    fun observeRecent(): Flow<List<TransactionEntity>> = transactionDao.observeRecent()

    fun observeFiltered(
        query: String,
        type: String,
        fromEpoch: Long,
        toEpoch: Long
    ): Flow<List<TransactionEntity>> = transactionDao.observeFiltered(query, type, fromEpoch, toEpoch)

    suspend fun syncFromSmsInbox(limit: Int = 400): Int = withContext(Dispatchers.IO) {
        val last = parserStateDao.get()?.lastSmsTimestamp ?: 0L
        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )
        val selection = "${Telephony.Sms.DATE} > ?"
        val selectionArgs = arrayOf(last.toString())
        val sort = "${Telephony.Sms.DATE} DESC LIMIT $limit"

        val cursor = contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI, projection, selection, selectionArgs, sort)
            ?: return@withContext 0

        var imported = 0
        var maxTimestamp = last
        cursor.use {
            val idxAddress = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val idxBody = it.getColumnIndexOrThrow(Telephony.Sms.BODY)
            val idxDate = it.getColumnIndexOrThrow(Telephony.Sms.DATE)

            while (it.moveToNext()) {
                val sender = it.getString(idxAddress).orEmpty()
                val body = it.getString(idxBody).orEmpty()
                val ts = it.getLong(idxDate)
                if (ts > maxTimestamp) maxTimestamp = ts

                val parser = bankParsers.firstOrNull { p -> p.canParse(sender, body) } ?: continue
                val parsed = parser.parse(sender, body, ts) ?: continue

                if (insertParsed(parsed)) {
                    imported++
                }
            }
        }

        if (maxTimestamp > last) {
            parserStateDao.upsert(ParserStateEntity(lastSmsTimestamp = maxTimestamp))
        }
        imported
    }

    private suspend fun insertParsed(parsed: ParsedTransaction): Boolean {
        val smsHash = sha256("${parsed.sender}|${ParserUtils.normalize(parsed.rawBody)}")
        if (transactionDao.duplicateCount(smsHash, parsed.amountIrr) > 0) return false

        val accountId = parsed.accountHint?.let {
            accountDao.findByHint(it)?.id ?: accountDao.insert(
                AccountEntity(
                    bankName = parsed.bankName,
                    accountHint = it
                )
            )
        }

        val entity = TransactionEntity(
            amountIrr = parsed.amountIrr,
            type = when (parsed.type) {
                TransactionType.CREDIT -> "CREDIT"
                TransactionType.DEBIT -> "DEBIT"
                TransactionType.CASH -> "CASH"
            },
            merchant = parsed.merchant,
            merchantNormalized = ParserUtils.normalize(parsed.merchant),
            accountId = accountId,
            categoryId = null,
            smsSender = parsed.sender,
            smsHash = smsHash,
            note = null,
            timestamp = parsed.occurredAt
        )

        return transactionDao.insert(entity) > 0
    }

    private fun sha256(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
