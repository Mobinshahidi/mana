package com.mana.tracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mana.tracker.data.backup.BackupManager
import com.mana.tracker.data.export.CsvExportManager
import com.mana.tracker.data.export.PdfExportManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsActionViewModel @Inject constructor(
    private val backupManager: BackupManager,
    private val csvExportManager: CsvExportManager,
    private val pdfExportManager: PdfExportManager
) : ViewModel() {

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status.asStateFlow()

    fun exportCsv() {
        viewModelScope.launch {
            val file = csvExportManager.exportCsv()
            _status.value = "CSV: ${file.name}"
        }
    }

    fun exportPdf() {
        viewModelScope.launch {
            val file = pdfExportManager.exportPdfPlaceholder()
            _status.value = "PDF: ${file.name}"
        }
    }

    fun backup() {
        viewModelScope.launch {
            val file = backupManager.createBackup()
            _status.value = "Backup: ${file.name}"
        }
    }
}
