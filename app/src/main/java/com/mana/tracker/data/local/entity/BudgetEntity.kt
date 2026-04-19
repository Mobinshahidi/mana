package com.mana.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val categoryId: Long?,
    val monthlyAmountIrr: Long,
    val budgetType: String,
    val monthKey: String,
    val createdAt: Long = System.currentTimeMillis()
)
