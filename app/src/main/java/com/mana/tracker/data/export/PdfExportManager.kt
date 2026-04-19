package com.mana.tracker.data.export

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class PdfExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun exportPdfPlaceholder(): File = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, "mana_report.pdf")
        file.writeBytes(byteArrayOf())
        file
    }
}
