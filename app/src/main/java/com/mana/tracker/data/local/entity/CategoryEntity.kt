package com.mana.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameFa: String,
    val nameEn: String,
    val icon: String,
    val colorHex: String,
    val isSystem: Boolean = false
)
