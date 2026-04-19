package com.mana.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mana.tracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE (:query = '' OR merchantNormalized LIKE '%' || :query || '%')
          AND (:type = '' OR type = :type)
          AND timestamp BETWEEN :fromEpoch AND :toEpoch
        ORDER BY timestamp DESC
        """
    )
    fun observeFiltered(
        query: String,
        type: String,
        fromEpoch: Long,
        toEpoch: Long
    ): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amountIrr), 0) FROM transactions WHERE type = 'DEBIT' AND timestamp BETWEEN :fromEpoch AND :toEpoch")
    suspend fun totalDebitBetween(fromEpoch: Long, toEpoch: Long): Long

    @Query("SELECT COALESCE(SUM(amountIrr), 0) FROM transactions WHERE type = 'CREDIT' AND timestamp BETWEEN :fromEpoch AND :toEpoch")
    suspend fun totalCreditBetween(fromEpoch: Long, toEpoch: Long): Long

    @Query("SELECT COUNT(*) FROM transactions WHERE smsHash = :smsHash AND amountIrr = :amountIrr")
    suspend fun duplicateCount(smsHash: String, amountIrr: Long): Int
}
