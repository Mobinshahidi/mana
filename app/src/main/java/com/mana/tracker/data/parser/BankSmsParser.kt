package com.mana.tracker.data.parser

interface BankSmsParser {
    val bankName: String
    fun canParse(sender: String, body: String): Boolean
    fun parse(sender: String, body: String, timestamp: Long): ParsedTransaction?
}
