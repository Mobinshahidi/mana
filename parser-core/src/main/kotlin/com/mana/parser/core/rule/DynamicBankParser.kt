package com.mana.parser.core.rule

import java.math.BigDecimal

class DynamicBankParser(private val config: BankConfig) {

    fun getBankName(): String = config.name

    fun canHandle(sender: String): Boolean {
        val upper = sender.uppercase()
        return config.senders.any { it.uppercase() == upper }
    }

    fun parse(message: String, sender: String? = null): ParseResult? {
        val cleanMessage = normalizePersianDigits(message.trim())

        if (!isTransactionMessage(cleanMessage)) return null

        val amount = extractAmount(cleanMessage)
        val type = detectType(cleanMessage)
        val merchant = extractMerchant(cleanMessage)
        val balance = extractField(cleanMessage, config.balancePatterns)
        val account = extractField(cleanMessage, config.accountPatterns)
        val reference = extractField(cleanMessage, config.referencePatterns)

        val confidence = calculateConfidence(cleanMessage, amount, type)

        return ParseResult(
            bankName = config.name,
            amount = amount,
            type = type,
            merchant = merchant,
            account = account,
            balance = balance,
            reference = reference,
            confidence = confidence,
            rawMessage = cleanMessage,
            detectedBySender = sender != null && canHandle(sender)
        )
    }

    private fun isTransactionMessage(message: String): Boolean {
        val lower = message.lowercase()
        for (key in config.excludeKeywords) {
            if (lower.contains(key.lowercase())) return false
        }
        if (config.transactionIndicators.isEmpty()) return true
        return config.transactionIndicators.any { lower.contains(it.lowercase()) }
    }

    private fun extractAmount(message: String): String? {
        for (rule in config.amountPatterns) {
            try {
                val pattern = Regex(rule.regex)
                val match = pattern.find(message) ?: continue
                val raw = match.groupValues.getOrNull(rule.group) ?: continue
                val cleaned = raw.replace(",", "").replace("+", "").replace("-", "").trim()
                if (cleaned.toBigDecimalOrNull()?.abs()?.compareTo(BigDecimal("999")) == 1) {
                    return cleaned
                }
            } catch (_: Exception) {}
        }
        return null
    }

    private fun detectType(message: String): String? {
        val lower = message.lowercase()

        for ((keyword, txnType) in config.typeDetection.keywords) {
            if (lower.contains(keyword.lowercase())) return txnType
        }

        val sign = config.typeDetection.signPosition ?: return null
        return when (sign) {
            "prefix" -> {
                for (rule in config.amountPatterns) {
                    try {
                        val pattern = Regex(rule.regex)
                        val m = pattern.find(message)
                        if (m != null) {
                            val full = m.groupValues.getOrNull(0) ?: continue
                            return if (full.startsWith("+")) "INCOME"
                            else if (full.startsWith("-")) "EXPENSE"
                            else null
                        }
                    } catch (_: Exception) {}
                }
                null
            }
            "suffix" -> {
                for (rule in config.amountPatterns) {
                    try {
                        val pattern = Regex(rule.regex)
                        val m = pattern.find(message)
                        if (m != null) {
                            val full = m.groupValues.getOrNull(0) ?: continue
                            return if (full.endsWith("+")) "INCOME"
                            else if (full.endsWith("-")) "EXPENSE"
                            else null
                        }
                    } catch (_: Exception) {}
                }
                null
            }
            else -> null
        }
    }

    private fun extractMerchant(message: String): String? {
        val lower = message.lowercase()
        for ((keyword, label) in config.merchantMap) {
            if (lower.contains(keyword.lowercase())) return label
        }
        return null
    }

    private fun extractField(message: String, rules: List<ExtractionRule>): String? {
        for (rule in rules) {
            try {
                val pattern = Regex(rule.regex)
                val match = pattern.find(message) ?: continue
                val value = match.groupValues.getOrNull(rule.group)?.trim()
                if (!value.isNullOrEmpty()) return value
            } catch (_: Exception) {}
        }
        return null
    }

    private fun calculateConfidence(message: String, amount: String?, type: String?): Float {
        var score = 0f
        if (amount != null) score += 0.4f
        if (type != null) score += 0.3f
        if (extractField(message, config.balancePatterns) != null) score += 0.2f
        if (config.transactionIndicators.any { message.lowercase().contains(it.lowercase()) }) score += 0.1f
        return score.coerceIn(0f, 1f)
    }

    private fun normalizePersianDigits(text: String): String {
        return text
            .replace('۰', '0').replace('٠', '0')
            .replace('۱', '1').replace('١', '1')
            .replace('۲', '2').replace('٢', '2')
            .replace('۳', '3').replace('٣', '3')
            .replace('۴', '4').replace('٤', '4')
            .replace('۵', '5').replace('٥', '5')
            .replace('۶', '6').replace('٦', '6')
            .replace('۷', '7').replace('٧', '7')
            .replace('۸', '8').replace('٨', '8')
            .replace('۹', '9').replace('٩', '9')
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .replace("\u200C", " ")
            .replace("\u200D", "")
            .replace("\u200E", "")
            .replace("\u200F", "")
            .trim()
    }

    fun matchScore(message: String): Float {
        var score = 0f
        for (indicator in config.transactionIndicators) {
            if (message.lowercase().contains(indicator.lowercase())) score += 0.3f
        }
        try {
            for (rule in config.amountPatterns) {
                if (Regex(rule.regex).containsMatchIn(message)) {
                    score += 0.4f
                    break
                }
            }
        } catch (_: Exception) {}
        for (keyword in config.typeDetection.keywords.keys) {
            if (message.lowercase().contains(keyword.lowercase())) score += 0.2f
        }
        return score
    }
}
