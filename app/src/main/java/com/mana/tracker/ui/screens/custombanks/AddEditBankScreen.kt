package com.mana.tracker.ui.screens.custombanks

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mana.tracker.ui.components.ManaCard
import com.mana.tracker.ui.components.ManaScaffold
import com.mana.tracker.ui.theme.Dimensions
import com.mana.tracker.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBankScreen(
    bankId: Long = -1L,
    onNavigateBack: () -> Unit,
    viewModel: AddEditBankViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(bankId) {
        viewModel.loadBank(bankId)
    }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onNavigateBack()
    }

    ManaScaffold(
        title = if (bankId > 0) "Edit Bank" else "Add Bank",
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimensions.Padding.content),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            ManaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Dimensions.Padding.content),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::updateName,
                        label = { Text("Bank Name") },
                        placeholder = { Text("e.g. My Bank") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.senderNumbers,
                        onValueChange = viewModel::updateSenderNumbers,
                        label = { Text("Sender Numbers") },
                        placeholder = { Text("e.g. +989123456789,+98212223344") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        supportingText = { Text("Comma-separated phone numbers") }
                    )

                    uiState.error?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = viewModel::save,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(Spacing.sm))
                        }
                        Text(if (bankId > 0) "Update" else "Save")
                    }
                }
            }

            ManaCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(Dimensions.Padding.content),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            "How to find sender numbers",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            "Open the SMS from your bank and look at the sender number. " +
                                    "Enter it here (with country code). You can add multiple numbers separated by commas.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
