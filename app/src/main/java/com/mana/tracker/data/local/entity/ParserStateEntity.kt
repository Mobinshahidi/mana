package com.mana.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parser_state")
data class ParserStateEntity(
    @PrimaryKey val id: Int = 1,
    val lastSmsTimestamp: Long = 0
)
