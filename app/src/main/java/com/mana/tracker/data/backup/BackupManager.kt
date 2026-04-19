package com.mana.tracker.data.backup

import android.content.Context
import com.mana.tracker.data.local.dao.TransactionDao
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionDao: TransactionDao
) {

    suspend fun createBackup(): File = withContext(Dispatchers.IO) {
        val file = File(context.cacheDir, "mana_backup.manabackup")
        val items = transactionDao.observeRecent(500)
        file.writeText("mana-backup-v1\n")
        file
    }
}
