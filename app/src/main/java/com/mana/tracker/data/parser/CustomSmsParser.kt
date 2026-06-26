package com.mana.tracker.data.parser

import com.mana.parser.core.TransactionType
import com.mana.parser.core.rule.SmartParseResult
import com.mana.tracker.data.database.entity.SmsTemplateEntity
import com.mana.tracker.data.repository.SmsTemplateRepository
import com.mana.tracker.data.repository.UserBankRepository
import org.json.JSONArray
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomSmsParser @Inject constructor(
    private val userBankRepository: UserBankRepository,
    private val smsTemplateRepository: SmsTemplateRepository
) {
    suspend fun parse(message: String, sender: String?): SmartParseResult? {
        if (sender.isNullOrBlank()) return null

        val bank = userBankRepository.getBankBySender(sender.uppercase()) ?: return null

        val templates = smsTemplateRepository.getActiveTemplatesForSender(sender.uppercase())
        if (templates.isEmpty()) return null

        for (template in templates) {
            val result = applyTemplate(template, message.trim())
            if (result != null) return result.copy(bankName = bank.name)
        }

        return null
    }

    private fun applyTemplate(template: SmsTemplateEntity, message: String): SmartParseResult? {
        val amount = tryMatch(template.amountRegex, message)?.toBigDecimalOrNull()
        val balance = tryMatch(template.balanceRegex, message)?.toBigDecimalOrNull()
        val merchant = tryMatch(template.merchantRegex, message)
        val account = tryMatch(template.accountRegex, message)
        val reference = tryMatch(template.referenceRegex, message)
        val isCard = message.contains("کارت", ignoreCase = true)

        val type = resolveType(template, message)

        if (amount == null && balance == null) return null

        return SmartParseResult(
            amount = amount,
            type = type,
            merchant = merchant,
            accountLast4 = account,
            balance = balance,
            reference = reference,
            isCardTransaction = isCard,
            bankName = null,
            confidence = calculateConfidence(amount, type, balance),
            rawMessage = message
        )
    }

    private fun resolveType(template: SmsTemplateEntity, message: String): TransactionType? {
        val keywordOverrides = template.typeKeywords?.let { parseTypeKeywords(it) } ?: emptyMap()
        val lower = message.lowercase()
        for ((keyword, type) in keywordOverrides) {
            if (lower.contains(keyword.lowercase())) return type
        }

        return template.transactionType?.let { typeName ->
            try {
                TransactionType.valueOf(typeName)
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }

    private fun parseTypeKeywords(json: String): Map<String, TransactionType> {
        return try {
            val arr = JSONArray(json)
            val map = mutableMapOf<String, TransactionType>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val keyword = obj.getString("keyword")
                val type = TransactionType.valueOf(obj.getString("type"))
                map[keyword] = type
            }
            map
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun tryMatch(regex: String?, text: String): String? {
        if (regex.isNullOrBlank()) return null
        return try {
            val pattern = Regex(regex, RegexOption.IGNORE_CASE)
            pattern.find(text)?.groupValues?.getOrNull(1)?.trim()
        } catch (_: Exception) {
            null
        }
    }

    private fun calculateConfidence(amount: BigDecimal?, type: TransactionType?, balance: BigDecimal?): Float {
        var score = 0f
        if (amount != null) score += 0.4f
        if (type != null) score += 0.35f
        if (balance != null) score += 0.25f
        return score.coerceIn(0f, 1f)
    }
}
