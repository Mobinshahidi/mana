package com.mana.tracker.data.export

import android.content.Context
import com.mana.tracker.data.local.dao.TransactionDao
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class CsvExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionDao: TransactionDao
) {

    suspend fun exportCsv(): File = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, "mana_transactions.csv")
        file.writeText("id,amountIrr,type,merchant,timestamp\n")
        file
    }
}
