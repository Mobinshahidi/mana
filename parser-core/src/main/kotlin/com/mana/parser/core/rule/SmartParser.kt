package com.mana.parser.core.rule

import com.mana.parser.core.TransactionType
import java.math.BigDecimal

data class SmartParseResult(
    val amount: BigDecimal?,
    val type: TransactionType?,
    val merchant: String?,
    val accountLast4: String?,
    val balance: BigDecimal?,
    val reference: String?,
    val isCardTransaction: Boolean,
    val bankName: String?,
    val confidence: Float,
    val rawMessage: String
)

data class BalanceInferenceResult(
    val type: TransactionType?,
    val gapDetected: Boolean,
    val gapAmount: BigDecimal?,
    val isBankPause: Boolean,
    val gapDirection: TransactionType? = null
)

object SmartParser {

    fun parse(message: String, sender: String? = null): SmartParseResult? {
        val clean = message.trim()
        if (clean.isEmpty()) return null

        val normalized = normalizePersianDigits(clean)
        val bankName = detectBank(sender, normalized)
        if (bankName == null && !looksLikeIranianBankMessage(normalized)) {
            return extractGeneric(normalized, null)
        }

        return extractFields(normalized, bankName)
    }

    fun classifyMessageType(message: String): String {
        val lower = normalizePersianDigits(message).lowercase()
        return when {
            containsAny(lower, listOf("otp", "code", "کد تایید", "رمز یکبار مصرف", "کد اعتبارسنجی")) -> "OTP / Verification"
            containsAny(lower, listOf("cashback", "offer", "discount", "تبلیغ", "تخفیف", "پیشنهاد")) -> "Promotional"
            containsAny(lower, listOf("مانده", "موجودی", "balance:")) &&
            !containsTransactionKeywords(lower) -> "Balance Inquiry"
            containsTransactionKeywords(lower) ||
            lower.contains("+") || lower.contains("-") -> "Transaction"
            containsAny(lower, listOf("قسط", "اشتراک", "subscription", "mandate")) -> "Subscription"
            else -> "Unknown"
        }
    }

    fun extractGeneric(message: String, bankName: String?): SmartParseResult {
        return extractFields(normalizePersianDigits(message), bankName)
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

    private fun extractFields(message: String, bankName: String?): SmartParseResult {
        val amount = extractAmount(message)
        val type = detectType(message)
        val merchant = extractMerchant(message)
        val balance = extractBalance(message)
        val account = extractAccount(message)
        val reference = extractReference(message)
        val isCard = message.contains("کارت", ignoreCase = true)

        val confidence = calculateConfidence(amount, type, balance)

        return SmartParseResult(
            amount = amount,
            type = type,
            merchant = merchant,
            accountLast4 = account,
            balance = balance,
            reference = reference,
            isCardTransaction = isCard,
            bankName = bankName,
            confidence = confidence,
            rawMessage = message
        )
    }

    private fun detectBank(sender: String?, message: String): String? {
        if (sender != null) {
            val upper = sender.uppercase()
            for ((bankName, senders) in BANK_SENDERS) {
                if (senders.any { upper.contains(it) }) return bankName
            }
        }

        val lower = message.lowercase()
        val scored = BANK_HINTS.mapValues { (_, hints) ->
            hints.count { lower.contains(it.lowercase()) }
        }
        return scored.maxByOrNull { it.value }?.takeIf { it.value > 0 }?.key
    }

    private fun looksLikeIranianBankMessage(message: String): Boolean {
        val lower = message.lowercase()
        if (containsAny(lower, IRANIAN_INDICATORS)) return true
        if (AMOUNT_REGEX.containsMatchIn(message)) return true
        if (message.contains("+") && message.contains(Regex("""[\d,]{4,}"""))) return true
        if (message.contains("-") && message.contains(Regex("""[\d,]{4,}"""))) return true
        return false
    }

    private fun containsTransactionKeywords(lower: String): Boolean {
        return containsAny(lower, TRANSACTION_KEYWORDS)
    }

    private fun containsAny(text: String, keywords: List<String>): Boolean {
        return keywords.any { text.contains(it.lowercase()) }
    }

    private fun extractAmount(message: String): BigDecimal? {
        for (pattern in AMOUNT_PATTERNS) {
            try {
                val match = pattern.find(message) ?: continue
                val raw = match.groupValues[1].replace(",", "").replace("+", "").replace("-", "").trim()
                val value = raw.toBigDecimalOrNull() ?: continue
                if (value.abs() >= BigDecimal("1000")) {
                    return value.abs()
                }
            } catch (_: Exception) {}
        }
        return null
    }

    private fun detectType(message: String): TransactionType? {
        val lower = message.lowercase()

        for ((keyword, type) in TYPE_KEYWORDS) {
            if (lower.contains(keyword)) return type
        }

        val leadingSign = LEADING_SIGN_REGEX.find(message)
        if (leadingSign != null) {
            return if (leadingSign.groupValues[1] == "+") TransactionType.INCOME
            else TransactionType.EXPENSE
        }

        val trailingSign = TRAILING_SIGN_REGEX.find(message)
        if (trailingSign != null) {
            return if (trailingSign.groupValues[1] == "+") TransactionType.INCOME
            else TransactionType.EXPENSE
        }

        return null
    }

    fun inferTypeFromBalance(
        amount: BigDecimal?,
        balance: BigDecimal?,
        knownPreviousBalance: BigDecimal?,
        tolerance: BigDecimal = BigDecimal.ZERO
    ): BalanceInferenceResult {
        if (amount == null || balance == null || knownPreviousBalance == null) {
            return BalanceInferenceResult(null, false, null, false)
        }
        val expectedAfterExpense = knownPreviousBalance - amount
        val expectedAfterIncome = knownPreviousBalance + amount

        for ((expected, direction) in listOf(expectedAfterExpense to TransactionType.EXPENSE, expectedAfterIncome to TransactionType.INCOME)) {
            val gap = (balance - expected).abs()
            if (gap == BigDecimal.ZERO) {
                return BalanceInferenceResult(direction, false, null, false, direction)
            }
            if (tolerance > BigDecimal.ZERO && gap <= tolerance) {
                return BalanceInferenceResult(direction, true, gap, false, direction)
            }
        }

        // Bank pause: balance delta is much larger than any transaction, meaning
        // multiple transactions happened without SMS. The remaining gap after
        // accounting for the known transaction must be significant (> 10x tolerance).
        val totalDelta = (balance - knownPreviousBalance).abs()
        if (totalDelta > amount && tolerance > BigDecimal.ZERO) {
            val remaining = totalDelta - amount
            if (remaining > tolerance * BigDecimal("10")) {
                val gapDir = if (balance > knownPreviousBalance) TransactionType.INCOME else TransactionType.EXPENSE
                return BalanceInferenceResult(null, true, remaining, true, gapDir)
            }
        }

        return BalanceInferenceResult(null, gapDetected = false, null, isBankPause = false)
    }

    private fun extractMerchant(message: String): String? {
        val lower = message.lowercase()
        for ((keyword, label) in MERCHANT_MAP) {
            if (lower.contains(keyword)) return label
        }

        val merchantPattern = Regex("""از\s*([\w\s]+?)(?:\s*$|\d|[\.،])""", RegexOption.IGNORE_CASE)
        merchantPattern.find(message)?.let { m ->
            val name = m.groupValues[1].trim()
            if (name.length in 2..30 && name.any { it.isLetter() }) return name
        }

        return null
    }

    private fun extractBalance(message: String): BigDecimal? {
        for (pattern in BALANCE_PATTERNS) {
            try {
                val match = pattern.find(message) ?: continue
                val raw = match.groupValues[1].replace(",", "").trim()
                val value = raw.toBigDecimalOrNull() ?: continue
                if (value >= BigDecimal.ZERO) return value
            } catch (_: Exception) {}
        }
        return null
    }

    private fun extractAccount(message: String): String? {
        val accountPattern = Regex("""حساب\s*:?\s*(\d{1,10})""")
        accountPattern.find(message)?.let { m ->
            val acct = m.groupValues[1]
            return if (acct.length >= 4) acct.takeLast(4) else acct
        }

        val cardPattern = Regex("""کارت(\d{4})\*""")
        cardPattern.find(message)?.let { m ->
            return m.groupValues[1]
        }

        val genericCardPattern = Regex("""(?:card|کارت)[^\d]*(\d{4})""", RegexOption.IGNORE_CASE)
        genericCardPattern.find(message)?.let { m ->
            return m.groupValues[1]
        }

        return null
    }

    private fun extractReference(message: String): String? {
        val refPattern = Regex("""(\d+\.\d+\.\d+)""")
        refPattern.find(message)?.let { m -> return m.groupValues[1] }

        val refPattern2 = Regex("""(?:ref|شماره پیگیری|پیگیری)[:\s]*(\d+)""", RegexOption.IGNORE_CASE)
        refPattern2.find(message)?.let { m ->
            val ref = m.groupValues[1]
            return if (ref.length >= 6) ref else null
        }

        return null
    }

    private fun calculateConfidence(amount: BigDecimal?, type: TransactionType?, balance: BigDecimal?): Float {
        var score = 0f
        if (amount != null) score += 0.4f
        if (type != null) {
            score += if (type == TransactionType.TRANSFER) 0.15f else 0.35f
        }
        if (balance != null) score += 0.25f
        return score.coerceIn(0f, 1f)
    }

    fun formatResult(result: SmartParseResult): String {
        val sb = StringBuilder()
        sb.appendLine("╔══════════════════════════════════════╗")
        sb.appendLine("║        SMS Parse Result              ║")
        sb.appendLine("╠══════════════════════════════════════╣")
        if (result.bankName != null) sb.appendLine("║ Bank:      ${result.bankName.padEnd(27)}║")
        sb.appendLine("║ Amount:    ${formatOrNA(result.amount?.toString()).padEnd(27)}║")
        sb.appendLine("║ Type:      ${formatOrNA(result.type?.name).padEnd(27)}║")
        sb.appendLine("║ Merchant:  ${formatOrNA(result.merchant).padEnd(27)}║")
        sb.appendLine("║ Account:   ${formatOrNA(result.accountLast4).padEnd(27)}║")
        sb.appendLine("║ Balance:   ${formatOrNA(result.balance?.toString()).padEnd(27)}║")
        sb.appendLine("║ Reference: ${formatOrNA(result.reference).padEnd(27)}║")
        sb.appendLine("║ Card TX:   ${(if (result.isCardTransaction) "Yes" else "No").padEnd(27)}║")
        sb.appendLine("║ Confidence: ${("${(result.confidence * 100).toInt()}%").padEnd(26)}║")
        sb.appendLine("╚══════════════════════════════════════╝")
        return sb.toString()
    }

    private fun formatOrNA(value: String?): String = value ?: "N/A"

    // === PATTERNS ===

    private val AMOUNT_REGEX = Regex("""[\d,]{4,}""")

    private val AMOUNT_PATTERNS = listOf(
        // Persian: مبلغ X ریال, X ریال, تومان
        Regex("""(?:مبلغ\s*)?([\d,]+)\s*(?:ریال|تومان|ريال|ريال)"""),
        Regex("""([\d,]+)\s*(?:ریال|تومان|ريال)"""),
        // Amount immediately after transaction keyword
        Regex("""(?:خرید|واريز|برداشت|واریز|انتقال|کارت|پرداخت|مصرف)\s*([+-]?[\d,]+)"""),
        // Leading sign: +X, -X
        Regex("""[+-]([\d,]+)"""),
        // Trailing sign: X+, X-
        Regex("""([\d,]+)[+-]"""),
        // Standalone comma-formatted number ≥ 1000
        Regex("""(\d{1,3}(?:,\d{3})+)(?:\s|$)"""),
        // English keywords
        Regex("""(?:amount|amt|mablagh)[:\s]*([\d,]+)""", RegexOption.IGNORE_CASE)
    )

    private val LEADING_SIGN_REGEX = Regex("""(?:^|\s)([+-])(?:[\d,]+)""")
    private val TRAILING_SIGN_REGEX = Regex("""(?:[\d,]+)([+-])(?:\s|$)""")

    private val TYPE_KEYWORDS: List<Pair<String, TransactionType>> = listOf(
        "واریز" to TransactionType.INCOME,
        "واريز" to TransactionType.INCOME,
        "deposit" to TransactionType.INCOME,
        "credited" to TransactionType.INCOME,
        "received" to TransactionType.INCOME,
        "refund" to TransactionType.INCOME,
        "return" to TransactionType.INCOME,
        "برداشت" to TransactionType.EXPENSE,
        "خرید" to TransactionType.EXPENSE,
        "پرداخت" to TransactionType.EXPENSE,
        "خريداينترنتي" to TransactionType.EXPENSE,
        "خرید اینترنتی" to TransactionType.EXPENSE,
        "مصرف" to TransactionType.EXPENSE,
        "کسر" to TransactionType.EXPENSE,
        "debit" to TransactionType.EXPENSE,
        "purchase" to TransactionType.EXPENSE,
        "payment" to TransactionType.EXPENSE,
        "withdrawn" to TransactionType.EXPENSE,
        "spent" to TransactionType.EXPENSE,
        "charged" to TransactionType.EXPENSE,
        "انتقال" to TransactionType.TRANSFER,
        "انتقالي" to TransactionType.TRANSFER,
        "transfer" to TransactionType.TRANSFER
    )

    private val MERCHANT_MAP = mapOf(
        "خريداينترنتي" to "Internet Purchase",
        "خرید اینترنتی" to "Internet Purchase",
        "واریز پول" to "Deposit",
        "خرید" to "Purchase",
        "انتقال" to "Transfer",
        "برداشت" to "Withdrawal",
        "انتقالي" to "Transfer",
        "واریز" to "Deposit",
        "واريز" to "Deposit",
        "کارت" to "Card Transaction",
        "پرداخت" to "Payment",
        "مصرف" to "Purchase",
        "کسر" to "Debit",
        "کسر از حساب" to "Debit"
    )

    private val BALANCE_PATTERNS = listOf(
        Regex("""(?:موجودی|مانده)\s*:?\s*([\d,]+)"""),
        Regex("""balance[:\s]*([\d,]+)""", RegexOption.IGNORE_CASE),
        Regex("""avl[.\s]*bal[:\s]*([\d,]+)""", RegexOption.IGNORE_CASE)
    )

    private val TRANSACTION_KEYWORDS = listOf(
        "واریز", "واريز", "برداشت", "خرید", "انتقال", "پرداخت",
        "خريداينترنتي", "خرید اینترنتی", "مصرف", "مانده", "موجودی",
        "ریال", "ريال", "تومان", "RIAL",
        "debit", "credit", "purchase", "payment", "transfer",
        "deposit", "withdrawal", "spent", "received"
    )

    private val IRANIAN_INDICATORS = listOf(
        "ریال", "ريال", "تومان", "RIAL", "مانده", "موجودی",
        "کارت", "حساب", "بانک", "واریز", "برداشت", "خرید",
        "انتقال", "بلو", "کشاورزی", "رفاه", "رسالت", "ملی",
        "پارسیان", "صادرات", "سپه", "تجارت", "پاسارگاد",
        "سامان", "اقتصاد نوین"
    )

    private val BANK_SENDERS = mapOf(
        "Melli Bank" to listOf("MELLI", "MELLIBANK", "98700717", "98700017"),
        "Parsian Bank" to listOf("PARSIAN", "PERSIAN"),
        "Resalat Bank" to listOf("RESALAT"),
        "Refah Bank" to listOf("REFAH", "RF-BANK"),
        "Keshavarzi Bank" to listOf("KESHAVARZI", "KESH", "BKI"),
        "Blu Bank" to listOf("BLU", "BLUBANK"),
        "Saderat Bank" to listOf("SADERAT"),
        "Tejarat Bank" to listOf("TEJARAT"),
        "Sepah Bank" to listOf("SEPAH"),
        "EN Bank" to listOf("EN-BANK", "EGHTESAD NOVIN"),
        "Pasargad Bank" to listOf("PASARGAD"),
        "Sarmayeh Bank" to listOf("SARMAYEH"),
        "Tosee Taavon Bank" to listOf("TOSEE TAAVON"),
        "Karafarin Bank" to listOf("KARAFARIN"),
        "Iran Zamin Bank" to listOf("IRAN ZAMIN"),
        "Hekmat Iranian Bank" to listOf("HEKMAT"),
        "Dey Bank" to listOf("DEY"),
        "Ayandeh Bank" to listOf("AYANDEH"),
        "Saman Bank" to listOf("SAMAN"),
        "Sina Bank" to listOf("SINA"),
        "Khavarmianeh Bank" to listOf("KHAVARMIANEH"),
        "Gardeshgari Bank" to listOf("GARDESHGARI"),
        "Post Bank" to listOf("POSTBANK", "POST BANK"),
        "Tosee Bank" to listOf("TOSEE"),
        "Ghavamin Bank" to listOf("GHAVAMIN"),
        "Shahr Bank" to listOf("SHAHR"),
        "Melal Bank" to listOf("MELAL"),
        "Ansar Bank" to listOf("ANSAR"),
        "Mehr Iran Bank" to listOf("MEHR IRAN"),
        "Kosar Credit" to listOf("KOSAR")
    )

    private val BANK_HINTS = mapOf(
        "Blu Bank" to listOf("بلو", "blu"),
        "Melli Bank" to listOf("ملی", "melli", "meli"),
        "Parsian Bank" to listOf("پارسیان", "parsian"),
        "Resalat Bank" to listOf("رسالت", "resalat"),
        "Refah Bank" to listOf("رفاه", "refah"),
        "Keshavarzi Bank" to listOf("کشاورزی", "keshavarzi", "bki"),
        "Saderat Bank" to listOf("صادرات", "saderat"),
        "Sepah Bank" to listOf("سپه", "sepah"),
        "Tejarat Bank" to listOf("تجارت", "tejarat"),
        "Pasargad Bank" to listOf("پاسارگاد", "pasargad"),
        "Saman Bank" to listOf("سامان", "saman"),
        "Ayandeh Bank" to listOf("آینده", "ayandeh"),
        "Shahr Bank" to listOf("شهر", "shahr"),
        "Post Bank" to listOf("پست", "post"),
        "Sina Bank" to listOf("سینا", "sina"),
        "En Bank" to listOf("اقتصاد نوین", "en bank")
    )
}
