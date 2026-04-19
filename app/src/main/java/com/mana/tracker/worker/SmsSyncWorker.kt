package com.mana.tracker.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mana.tracker.data.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SmsSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val transactionRepository: TransactionRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val permissionGranted = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) return Result.success()

        return runCatching {
            transactionRepository.syncFromSmsInbox()
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "sms_sync_worker"
    }
}
