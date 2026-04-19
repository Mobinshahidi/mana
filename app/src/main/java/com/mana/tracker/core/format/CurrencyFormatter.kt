package com.mana.tracker.core.format

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {

    fun formatIrr(amount: Long, localeTag: String): String {
        val locale = Locale.forLanguageTag(localeTag)
        val symbols = DecimalFormatSymbols(locale)
        val formatter = DecimalFormat("#,###", symbols)
        val raw = formatter.format(amount)
        return if (locale.language == "fa") "$raw ریال" else "$raw IRR"
    }
}
