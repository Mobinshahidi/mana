package com.mana.tracker.data.parser

object IranianBankParsers {

    private val commonAmountRegexes = listOf(
        Regex("مبلغ\\s*[:：-]?\\s*([0-9۰-۹,٬]+)\\s*ریال"),
        Regex("برداشت[^0-9۰-۹]*([0-9۰-۹,٬]+)\\s*ریال"),
        Regex("واریز[^0-9۰-۹]*([0-9۰-۹,٬]+)\\s*ریال")
    )

    val all: List<BankSmsParser> = listOf(
        PatternBankParser("Mellat Bank", listOf("Mellat", "بانک ملت"), listOf("ملت", "تراکنش"), commonAmountRegexes),
        PatternBankParser("Melli Iran", listOf("Melli", "بانک ملی"), listOf("ملی", "واریز", "برداشت"), commonAmountRegexes),
        PatternBankParser("Sepah", listOf("Sepah", "بانک سپه"), listOf("سپه", "ریال"), commonAmountRegexes),
        PatternBankParser("Tejarat", listOf("Tejarat", "بانک تجارت"), listOf("تجارت", "مبلغ"), commonAmountRegexes),
        PatternBankParser("Saderat Iran", listOf("Saderat", "بانک صادرات"), listOf("صادرات", "برداشت"), commonAmountRegexes),
        PatternBankParser("Pasargad", listOf("Pasargad", "بانک پاسارگاد"), listOf("پاسارگاد", "واریز"), commonAmountRegexes),
        PatternBankParser("Saman", listOf("Saman", "بانک سامان"), listOf("سامان", "تراکنش"), commonAmountRegexes),
        PatternBankParser("Karafarin", listOf("Karafarin", "بانک کارآفرین"), listOf("کارآفرین", "ریال"), commonAmountRegexes),
        PatternBankParser("Parsian", listOf("Parsian", "بانک پارسیان"), listOf("پارسیان", "برداشت"), commonAmountRegexes),
        PatternBankParser("Shahr Bank", listOf("Shahr", "بانک شهر"), listOf("بانک شهر", "واریز"), commonAmountRegexes),
        PatternBankParser("Qavamin", listOf("Qavamin", "بانک قوامین"), listOf("قوامین", "ریال"), commonAmountRegexes),
        PatternBankParser("Mehr Iran", listOf("Mehr", "بانک مهر ایران"), listOf("مهر ایران", "مبلغ"), commonAmountRegexes),
        PatternBankParser("Day Bank", listOf("Day", "بانک دی"), listOf("بانک دی", "تراکنش"), commonAmountRegexes),
        PatternBankParser("Tose'e Ta'avon", listOf("Taavon", "بانک توسعه تعاون"), listOf("توسعه تعاون", "ریال"), commonAmountRegexes)
    )
}
