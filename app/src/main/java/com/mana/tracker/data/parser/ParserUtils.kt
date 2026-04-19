package com.mana.tracker.data.parser

import com.mana.tracker.domain.model.TransactionType
import java.text.Normalizer

object ParserUtils {

    private val amountPattern = Regex("([0-9۰-۹][0-9۰-۹,٬]*)\\s*(?:ریال|rial|IRR)", RegexOption.IGNORE_CASE)
    private val accountPattern = Regex("(?:حساب|کارت|شماره)\\s*[:：-]?\\s*([0-9۰-۹*]{4,})")
    private val merchantPattern = Regex("(?:فروشگاه|پذیرنده|به\\s+نام|درگاه)\\s*[:：-]?\\s*([\\p{L}0-9 _-]{2,})")

    fun normalize(input: String): String {
        return Normalizer.normalize(input, Normalizer.Form.NFKC)
            .replace('ي', 'ی')
            .replace('ك', 'ک')
            .trim()
            .lowercase()
    }

    fun parseAmount(body: String): Long? {
        val match = amountPattern.find(body) ?: return null
        val normalizedDigits = toEnglishDigits(match.groupValues[1]).replace(",", "").replace("٬", "")
        return normalizedDigits.toLongOrNull()
    }

    fun parseAmountWithRegex(body: String, regexes: List<Regex>): Long? {
        for (regex in regexes) {
            val group = regex.find(body)?.groupValues?.getOrNull(1) ?: continue
            val normalized = toEnglishDigits(group).replace(",", "").replace("٬", "")
            val parsed = normalized.toLongOrNull()
            if (parsed != null) return parsed
        }
        return null
    }

    fun parseAccountHint(body: String): String? {
        return accountPattern.find(body)?.groupValues?.getOrNull(1)?.let(::toEnglishDigits)
    }

    fun parseMerchant(body: String): String {
        return merchantPattern.find(body)?.groupValues?.getOrNull(1)?.trim().orEmpty().ifBlank {
            when {
                body.contains("برداشت") -> "برداشت"
                body.contains("واریز") -> "واریز"
                else -> "تراکنش"
            }
        }
    }

    fun parseType(body: String): TransactionType {
        return when {
            body.contains("واریز") || body.contains("بستانکار") -> TransactionType.CREDIT
            else -> TransactionType.DEBIT
        }
    }

    fun toEnglishDigits(value: String): String {
        val map = mapOf(
            '۰' to '0', '۱' to '1', '۲' to '2', '۳' to '3', '۴' to '4',
            '۵' to '5', '۶' to '6', '۷' to '7', '۸' to '8', '۹' to '9'
        )
        return value.map { map[it] ?: it }.joinToString("")
    }
}
