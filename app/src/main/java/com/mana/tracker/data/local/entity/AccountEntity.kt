package com.mana.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bankName: String,
    val accountHint: String,
    val currentBalanceIrr: Long = 0,
    val updatedAt: Long = System.currentTimeMillis()
)
