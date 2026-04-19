package com.mana.tracker.core.i18n

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleManager {
    fun apply(languageTag: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
    }

    fun currentTag(context: Context): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        if (!locales.isEmpty) return locales.toLanguageTags()
        return context.resources.configuration.locales[0].toLanguageTag()
    }
}
