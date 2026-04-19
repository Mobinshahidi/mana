package com.mana.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "balance_history",
    indices = [Index("accountId"), Index("timestamp")]
)
data class BalanceHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val balanceIrr: Long,
    val timestamp: Long
)
