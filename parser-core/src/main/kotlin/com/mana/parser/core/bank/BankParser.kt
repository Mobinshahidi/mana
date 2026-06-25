package com.mana.parser.core.bank

import com.mana.parser.core.CompiledPatterns
import com.mana.parser.core.Constants
import com.mana.parser.core.ParsedTransaction
import com.mana.parser.core.TransactionType
import java.math.BigDecimal

/**
 * Base class for bank-specific message parsers.
 * Each bank should extend this class and implement its specific parsing logic.
 */
abstract class BankParser {

    /**
     * Returns the name of the bank this parser handles.
     */
    abstract fun getBankName(): String

    /**
     * Checks if this parser can handle messages from the given sender.
     */
    abstract fun canHandle(sender: String): Boolean

    /**
     * Returns the currency used by this bank.
     * All banks use IRR (Iranian Rial).
     */
    open fun getCurrency(): String = "IRR"

    /**
     * Parses an SMS message and extracts transaction information.
     * Returns null if the message cannot be parsed.
     */
    open fun parse(smsBody: String, sender: String, timestamp: Long): ParsedTransaction? {
        // Skip non-transaction messages
        if (!isTransactionMessage(smsBody)) {
            return null
        }

        val amount = extractAmount(smsBody)
        if (amount == null) {
            return null
        }

        val type = extractTransactionType(smsBody)
        if (type == null) {
            return null
        }

        return ParsedTransaction(
            amount = amount,
            type = type,
            merchant = extractMerchant(smsBody, sender),
            reference = extractReference(smsBody),
            accountLast4 = extractAccountLast4(smsBody),
            balance = extractBalance(smsBody),
            smsBody = smsBody,
            sender = sender,
            timestamp = timestamp,
            bankName = getBankName(),
            isFromCard = detectIsCard(smsBody),
            currency = getCurrency()
        )
    }

    /**
     * Checks if the message is a transaction message (not OTP, promotional, etc.)
     */
    protected open fun isTransactionMessage(message: String): Boolean {
        val lowerMessage = message.lowercase()

        // Skip OTP messages
        if (lowerMessage.contains("otp") ||
            lowerMessage.contains("one time password") ||
            lowerMessage.contains("verification code") ||
            lowerMessage.contains("رمز یکبار مصرف") ||
            lowerMessage.contains("کد تایید")
        ) {
            return false
        }

        // Skip promotional messages
        if (lowerMessage.contains("offer") ||
            lowerMessage.contains("discount") ||
            lowerMessage.contains("cashback offer") ||
            lowerMessage.contains("win ") ||
            lowerMessage.contains("تبلیغ") ||
            lowerMessage.contains("پیشنهاد") ||
            lowerMessage.contains("تخفیف")
        ) {
            return false
        }

        return true
    }

    /**
     * Extracts the transaction currency from the message.
     */
    protected open fun extractCurrency(message: String): String? {
        return "IRR"
    }

    /**
     * Extracts the transaction amount from the message.
     */
    protected open fun extractAmount(message: String): BigDecimal? {
        for (pattern in CompiledPatterns.Amount.ALL_PATTERNS) {
            pattern.find(message)?.let { match ->
                val amountStr = match.groupValues[1].replace(",", "")
                return try {
                    BigDecimal(amountStr)
                } catch (e: NumberFormatException) {
                    null
                }
            }
        }

        return null
    }

    /**
     * Extracts the transaction type (INCOME/EXPENSE/INVESTMENT).
     */
    protected open fun extractTransactionType(message: String): TransactionType? {
        val lowerMessage = message.lowercase()

        // Check for investment transactions first (highest priority)
        if (isInvestmentTransaction(lowerMessage)) {
            return TransactionType.INVESTMENT
        }

        return when {
            lowerMessage.contains("debited") -> TransactionType.EXPENSE
            lowerMessage.contains("withdrawn") -> TransactionType.EXPENSE
            lowerMessage.contains("spent") -> TransactionType.EXPENSE
            lowerMessage.contains("charged") -> TransactionType.EXPENSE
            lowerMessage.contains("paid") -> TransactionType.EXPENSE
            lowerMessage.contains("purchase") -> TransactionType.EXPENSE
            lowerMessage.contains("deducted") -> TransactionType.EXPENSE

            lowerMessage.contains("credited") -> TransactionType.INCOME
            lowerMessage.contains("deposited") -> TransactionType.INCOME
            lowerMessage.contains("received") -> TransactionType.INCOME
            lowerMessage.contains("refund") -> TransactionType.INCOME
            lowerMessage.contains("cashback") && !lowerMessage.contains("earn cashback") -> TransactionType.INCOME

            else -> null
        }
    }

    /**
     * Checks if the message is for an investment transaction.
     */
    protected open fun isInvestmentTransaction(lowerMessage: String): Boolean {
        return false
    }

    /**
     * Extracts merchant/payee information.
     */
    protected open fun extractMerchant(message: String, sender: String): String? {
        for (pattern in CompiledPatterns.Merchant.ALL_PATTERNS) {
            pattern.find(message)?.let { match ->
                val merchant = cleanMerchantName(match.groupValues[1].trim())
                if (isValidMerchantName(merchant)) {
                    return merchant
                }
            }
        }

        return null
    }

    /**
     * Extracts transaction reference number.
     */
    protected open fun extractReference(message: String): String? {
        for (pattern in CompiledPatterns.Reference.ALL_PATTERNS) {
            pattern.find(message)?.let { match ->
                return match.groupValues[1].trim()
            }
        }

        return null
    }

    /**
     * Extracts last 4 digits of account number.
     */
    protected open fun extractAccountLast4(message: String): String? {
        for (pattern in CompiledPatterns.Account.ALL_PATTERNS) {
            pattern.find(message)?.let { match ->
                val accountLast4 = match.groupValues[1]

                // Validate that this is actually an account number, not a date or RRN
                if (isValidAccountLast4(accountLast4, match.value, message)) {
                    return accountLast4
                }
            }
        }

        return null
    }

    /**
     * Validates that the extracted 4 digits are actually part of an account number,
     * not a date, RRN, or other numeric field.
     */
    private fun isValidAccountLast4(last4: String, matchedText: String, fullMessage: String): Boolean {
        // Escape the last4 for safe regex usage
        val escapedLast4 = Regex.escape(last4)

        // Check if it's part of a date pattern (dd/mm/yyyy, dd-mm-yyyy, etc.)
        val datePatterns = listOf(
            Regex("""\d{1,2}[/-]\d{1,2}[/-]$escapedLast4"""),  // 04/11/2025, 05-02-2025
            Regex("""$escapedLast4[/-]\d{1,2}[/-]\d{1,2}"""),  // 2025/11/04, 2025-02-05
            Regex("""\bon\s+\d{1,2}[/-]\d{1,2}[/-]$escapedLast4""", RegexOption.IGNORE_CASE),  // "on 04/11/2025"
            Regex("""\bdated\s+\d{1,2}[/-]\d{1,2}[/-]$escapedLast4""", RegexOption.IGNORE_CASE)  // "dated 05-02-2025"
        )

        for (datePattern in datePatterns) {
            if (datePattern.find(fullMessage) != null) {
                return false
            }
        }

        // Check if it's part of an RRN (Reference Number) - typically 12 digits
        val rrnPatterns = listOf(
            Regex("""RRN\s+(?:No\.?)?(\d{8,16})""", RegexOption.IGNORE_CASE),  // "RRN No.503612315893"
            Regex("""Ref\s+(?:No\.?)?(\d{8,16})""", RegexOption.IGNORE_CASE)   // "Ref No.503612315893"
        )

        for (rrnPattern in rrnPatterns) {
            rrnPattern.find(fullMessage)?.let { rrnMatch ->
                val rrnNumber = rrnMatch.groupValues[1]
                // If our last4 is part of this RRN, reject it
                if (rrnNumber.contains(last4)) {
                    return false
                }
            }
        }

        // Check if it's a standalone year (2024, 2025, etc.)
        if (last4.toIntOrNull() in 2000..2099) {
            // Only reject if it appears to be a year in date context
            val yearContextPatterns = listOf(
                Regex("""\bon\s+\d{1,2}[/-]\d{1,2}[/-]$escapedLast4""", RegexOption.IGNORE_CASE),
                Regex("""\bdated\s+.*?$escapedLast4""", RegexOption.IGNORE_CASE),
                Regex("""$escapedLast4(?:\s|$)""")  // Year at end of phrase
            )

            for (yearPattern in yearContextPatterns) {
                if (yearPattern.find(fullMessage) != null) {
                    // Only reject if NOT preceded by "Account" or "A/c" within 25 chars
                    val accountBeforeYear = Regex("""(?:A/c|Account|Acct).{0,25}$escapedLast4""", RegexOption.IGNORE_CASE)
                    if (accountBeforeYear.find(fullMessage) == null) {
                        return false
                    }
                }
            }
        }

        return true
    }

    /**
     * Extracts balance after transaction.
     */
    protected open fun extractBalance(message: String): BigDecimal? {
        for (pattern in CompiledPatterns.Balance.ALL_PATTERNS) {
            pattern.find(message)?.let { match ->
                val balanceStr = match.groupValues[1].replace(",", "")
                return try {
                    BigDecimal(balanceStr)
                } catch (e: NumberFormatException) {
                    null
                }
            }
        }

        return null
    }

    /**
     * Extracts available limit from the message.
     */
    protected open fun extractAvailableLimit(message: String): BigDecimal? {
        return null
    }

    /**
     * Detects if the transaction is from a card.
     */
    protected open fun detectIsCard(message: String): Boolean {
        return message.contains("کارت", ignoreCase = true)
    }

    /**
     * Cleans merchant name by removing common suffixes and noise.
     */
    protected open fun cleanMerchantName(merchant: String): String {
        return merchant
            .replace(CompiledPatterns.Cleaning.TRAILING_PARENTHESES, "")
            .replace(CompiledPatterns.Cleaning.REF_NUMBER_SUFFIX, "")
            .replace(CompiledPatterns.Cleaning.DATE_SUFFIX, "")
            .replace(CompiledPatterns.Cleaning.TIME_SUFFIX, "")
            .replace(CompiledPatterns.Cleaning.TRAILING_DASH, "")
            .trim()
    }

    /**
     * Validates if the extracted merchant name is valid.
     */
    protected open fun isValidMerchantName(name: String): Boolean {
        return name.length >= Constants.Parsing.MIN_MERCHANT_NAME_LENGTH &&
                name.any { it.isLetter() } &&
                !name.all { it.isDigit() }
    }
}
