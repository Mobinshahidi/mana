package com.mana.tracker.data.parser

import com.mana.tracker.domain.model.TransactionType

data class ParsedTransaction(
    val bankName: String,
    val amountIrr: Long,
    val type: TransactionType,
    val merchant: String,
    val accountHint: String?,
    val occurredAt: Long,
    val rawBody: String,
    val sender: String
)
