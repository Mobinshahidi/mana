package com.mana.tracker.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mana.tracker.R
import com.mana.tracker.core.security.BiometricLockManager
import com.mana.tracker.ui.viewmodel.SettingsActionViewModel

@Composable
fun SettingsScreen(
    currentLocaleTag: String,
    onSetLocale: (String) -> Unit,
    biometricLockManager: BiometricLockManager,
    actionsViewModel: SettingsActionViewModel = hiltViewModel()
) {
    val actionStatus by actionsViewModel.status.collectAsStateWithLifecycle()
    val biometricText = if (biometricLockManager.isBiometricAvailable()) {
        stringResource(R.string.biometric_available)
    } else {
        stringResource(R.string.biometric_unavailable)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.nav_settings), style = MaterialTheme.typography.headlineSmall)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.settings_language))
                Text(stringResource(R.string.settings_current, currentLocaleTag))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onSetLocale("fa") }) { Text("فارسی") }
                    Button(onClick = { onSetLocale("en") }) { Text("English") }
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(biometricText)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = actionsViewModel::exportCsv) { Text(stringResource(R.string.export_csv)) }
                    Button(onClick = actionsViewModel::exportPdf) { Text(stringResource(R.string.export_pdf)) }
                }
                Button(onClick = actionsViewModel::backup) { Text(stringResource(R.string.backup_restore)) }
                if (actionStatus.isNotBlank()) {
                    Text(actionStatus)
                }
            }
        }
    }
}
