package com.mana.parser.core.rule

class MessageClassifier(private val parsers: List<DynamicBankParser>) {

    data class Classification(
        val bankName: String,
        val messageType: String,
        val confidence: Float
    )

    fun classify(message: String, sender: String? = null): Classification? {
        val cleanMessage = message.trim()
        if (cleanMessage.isEmpty()) return null

        if (sender != null) {
            val directParser = parsers.firstOrNull { it.canHandle(sender) }
            if (directParser != null) {
                val result = directParser.parse(cleanMessage, sender) ?: return null
                return Classification(result.bankName, result.type ?: "UNKNOWN", result.confidence)
            }
        }

        val scored = parsers.mapNotNull { parser ->
            val result = parser.parse(cleanMessage)
            if (result != null) {
                Classification(result.bankName, result.type ?: "UNKNOWN", result.confidence)
            } else {
                val matchScore = parser.matchScore(cleanMessage)
                if (matchScore > 0.3f) {
                    Classification(parser.getBankName(), "UNKNOWN", matchScore)
                } else null
            }
        }.sortedByDescending { it.confidence }

        return scored.firstOrNull()
    }

    fun parseAll(message: String, sender: String? = null): List<ParseResult> {
        return parsers.mapNotNull { parser ->
            if (sender != null && !parser.canHandle(sender) && sender.isNotEmpty()) {
                null
            } else {
                parser.parse(message, sender)
            }
        }.sortedByDescending { it.confidence }
    }

    fun classifyMessageType(message: String): String {
        val lower = message.lowercase()
        return when {
            lower.contains("otp") || lower.contains("کد تایید") || lower.contains("رمز") -> "OTP"
            lower.contains("مانده") || lower.contains("موجودی") -> "BALANCE_UPDATE"
            lower.contains("واریز") || lower.contains("برداشت") ||
            lower.contains("خرید") || lower.contains("انتقال") ||
            lower.contains("پرداخت") -> "TRANSACTION"
            lower.contains("قسط") || lower.contains("اشتراک") -> "SUBSCRIPTION"
            message.contains("+") || message.contains("-") -> "TRANSACTION"
            else -> "UNKNOWN"
        }
    }
}
