package com.mana.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val merchantPattern: String,
    val minAmountIrr: Long?,
    val maxAmountIrr: Long?,
    val assignCategoryId: Long?,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
