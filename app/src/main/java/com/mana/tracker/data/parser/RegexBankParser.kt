package com.mana.tracker.data.parser

class RegexBankParser(
    override val bankName: String,
    private val senderHints: List<String>,
    private val bodyHints: List<String>
) : BankSmsParser {

    override fun canParse(sender: String, body: String): Boolean {
        val s = ParserUtils.normalize(sender)
        val b = ParserUtils.normalize(body)
        return senderHints.any { s.contains(ParserUtils.normalize(it)) } ||
            bodyHints.any { b.contains(ParserUtils.normalize(it)) }
    }

    override fun parse(sender: String, body: String, timestamp: Long): ParsedTransaction? {
        val amount = ParserUtils.parseAmount(body) ?: return null
        return ParsedTransaction(
            bankName = bankName,
            amountIrr = amount,
            type = ParserUtils.parseType(body),
            merchant = ParserUtils.parseMerchant(body),
            accountHint = ParserUtils.parseAccountHint(body),
            occurredAt = timestamp,
            rawBody = body,
            sender = sender
        )
    }
}
