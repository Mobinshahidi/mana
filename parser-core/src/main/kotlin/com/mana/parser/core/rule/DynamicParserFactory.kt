package com.mana.parser.core.rule

object DynamicParserFactory {

    fun createClassifier(banks: List<BankConfig>): MessageClassifier {
        val parsers = banks.map { DynamicBankParser(it) }
        return MessageClassifier(parsers)
    }

    fun createParser(config: BankConfig): DynamicBankParser = DynamicBankParser(config)

    fun loadDefault(): MessageClassifier {
        return createClassifier(buildDefaultConfigs())
    }

    fun buildDefaultConfigs(): List<BankConfig> {
        return listOf(
            BankConfig(
                name = "Blu Bank",
                senders = listOf("BLU", "BLUBANK", "BLU BANK"),
                currency = "IRR",
                amountPatterns = listOf(ExtractionRule("""([\d,]+)\s*ریال""")),
                typeDetection = TypeDetectionConfig(keywords = mapOf("واریز" to "INCOME", "برداشت" to "EXPENSE")),
                merchantMap = mapOf("واریز پول" to "Deposit", "برداشت" to "Withdrawal"),
                balancePatterns = listOf(ExtractionRule("""موجودی[:]?\s*([\d,]+)""")),
                transactionIndicators = listOf("بلو", "واریز", "موجودی"),
                excludeKeywords = listOf("otp", "کد تایید", "رمز")
            ),
            BankConfig(
                name = "Keshavarzi Bank",
                senders = listOf("KESHAVARZI", "KESH", "BKI"),
                currency = "IRR",
                amountPatterns = listOf(ExtractionRule("""(خرید|واريز|برداشت)\s*([\d,]+)""", group = 2)),
                typeDetection = TypeDetectionConfig(keywords = mapOf("واريز" to "INCOME", "خرید" to "EXPENSE", "برداشت" to "EXPENSE")),
                merchantMap = mapOf("خرید" to "Purchase", "واريز" to "Deposit", "برداشت" to "Withdrawal"),
                balancePatterns = listOf(ExtractionRule("""مانده\s*([\d,]+)""")),
                accountPatterns = listOf(ExtractionRule("""کارت(\d{4})\*""")),
                transactionIndicators = listOf("خرید", "واريز", "برداشت", "مانده"),
                excludeKeywords = listOf("otp", "کد تایید", "رمز")
            ),
            BankConfig(
                name = "Refah Bank",
                senders = listOf("REFAH", "RF-BANK", "REF.BANK", "BANK REFAH", "بانک رفاه"),
                currency = "IRR",
                amountPatterns = listOf(ExtractionRule("""(?:کارت|خرید|برداشت|واریز)\s*([\d,]+)[+-]""")),
                typeDetection = TypeDetectionConfig(keywords = mapOf("واریز" to "INCOME"), signPosition = "suffix"),
                merchantMap = mapOf("خرید" to "Purchase", "برداشت" to "Withdrawal", "کارت" to "Card Transaction", "واریز" to "Deposit"),
                balancePatterns = listOf(ExtractionRule("""مانده\s*([\d,]+)""")),
                accountPatterns = listOf(ExtractionRule("""حساب\s*(\d+)""")),
                transactionIndicators = listOf("بانک رفاه", "حساب", "مانده", "کارت"),
                excludeKeywords = listOf("otp", "کد تایید", "رمز")
            ),
            BankConfig(
                name = "Resalat Bank",
                senders = listOf("RESALAT", "RESALAT BANK"),
                currency = "IRR",
                amountPatterns = listOf(ExtractionRule("""[+-]([\d,]+)""")),
                typeDetection = TypeDetectionConfig(signPosition = "prefix"),
                balancePatterns = listOf(ExtractionRule("""مانده[:]?\s*([\d,]+)""")),
                referencePatterns = listOf(ExtractionRule("""(\d+\.\d+\.\d+)""")),
                transactionIndicators = listOf("+", "-"),
                excludeKeywords = listOf("otp", "کد تایید", "رمز")
            ),
            BankConfig(
                name = "Melli Bank",
                senders = listOf("+98700717", "+98700017", "MELLI", "MELLIBANK", "MELLI BANK", "BANK MELLI", "BANKMELLI"),
                currency = "IRR",
                amountPatterns = listOf(
                    ExtractionRule("""(?:خريداينترنتي|انتقال|برداشت|انتقالي|واریز|خرید):\s*([+-]?[\d,]+(?:\.\d{1,2})?)""", group = 2),
                    ExtractionRule("""(?:مبلغ\s*)?([\d,]+)(?:\s*(?:ریال|تومان))?\s*(?:برداشت|واریز|انتقال|خرید|[-+])""")
                ),
                typeDetection = TypeDetectionConfig(
                    keywords = mapOf("واریز" to "INCOME", "برداشت" to "EXPENSE", "خرید" to "EXPENSE", "انتقال" to "EXPENSE", "پرداخت" to "EXPENSE", "خريداينترنتي" to "EXPENSE"),
                    signPosition = "suffix"
                ),
                merchantMap = mapOf("خريداينترنتي" to "Internet Purchase", "خرید اینترنتی" to "Internet Purchase", "خرید" to "Purchase", "انتقال" to "Transfer", "برداشت" to "Withdrawal", "انتقالي" to "Transfer", "واریز" to "Deposit"),
                balancePatterns = listOf(ExtractionRule("""مانده\s*:?\s*([\d,]+)""")),
                accountPatterns = listOf(ExtractionRule("""حساب\s*:?\s*(\d{1,5})""")),
                transactionIndicators = listOf("خرید", "انتقال", "برداشت", "واریز", "ریال"),
                excludeKeywords = listOf("otp", "کد تایید", "رمز")
            ),
            BankConfig(
                name = "Parsian Bank",
                senders = listOf("PARSIAN", "PERSIAN", "PARSIANBANK"),
                currency = "IRR",
                amountPatterns = listOf(
                    ExtractionRule("""(?:خريداينترنتي|انتقال|برداشت|انتقالي|واریز|خرید):\s*([+-]?[\d,]+(?:\.\d{1,2})?)""", group = 2),
                    ExtractionRule("""[+-]([\d,]+)""")
                ),
                typeDetection = TypeDetectionConfig(keywords = mapOf("واریز" to "INCOME", "برداشت" to "EXPENSE", "خرید" to "EXPENSE"), signPosition = "prefix"),
                merchantMap = mapOf("خرید" to "Purchase", "انتقال" to "Transfer", "برداشت" to "Withdrawal", "واریز" to "Deposit"),
                balancePatterns = listOf(ExtractionRule("""مانده\s*:?\s*([\d,]+)""")),
                accountPatterns = listOf(ExtractionRule("""(\d{4})[-\s]?(\d{4})""")),
                transactionIndicators = listOf("خرید", "انتقال", "برداشت", "واریز"),
                excludeKeywords = listOf("otp", "کد تایید", "رمز")
            )
        )
    }
}
