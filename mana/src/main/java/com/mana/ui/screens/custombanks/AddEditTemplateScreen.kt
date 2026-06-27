package com.mana.ui.screens.custombanks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mana.ui.components.ManaCard
import com.mana.ui.components.ManaScaffold
import com.mana.ui.theme.Dimensions
import com.mana.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTemplateScreen(
    bankId: Long,
    templateId: Long = -1L,
    onNavigateBack: () -> Unit,
    viewModel: AddEditTemplateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(bankId, templateId) {
        viewModel.init(bankId, templateId)
    }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onNavigateBack()
    }

    ManaScaffold(
        title = if (templateId > 0) "Edit Template" else "Add Template",
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
                .padding(Dimensions.Padding.content)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Basic Info
            ManaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Dimensions.Padding.content),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text("Basic Info", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)

                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::updateName,
                        label = { Text("Template Name") },
                        placeholder = { Text("e.g. Deposit SMS") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    var typeExpanded by remember { mutableStateOf(false) }
                    val typeOptions = listOf(null, "INCOME", "EXPENSE", "TRANSFER")
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = uiState.transactionType ?: "Auto-detect",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Default Transaction Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            typeOptions.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t ?: "Auto-detect") },
                                    onClick = { viewModel.updateTransactionType(t); typeExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            // Type Keywords
            ManaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Dimensions.Padding.content),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text("Type Keywords", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Text(
                        "Keywords in the SMS that determine the transaction type",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.typeKeywordKey,
                            onValueChange = viewModel::updateTypeKeywordKey,
                            label = { Text("Keyword") },
                            placeholder = { Text("واریز") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        var kwTypeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = kwTypeExpanded,
                            onExpandedChange = { kwTypeExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = if (uiState.typeKeywordValue.isEmpty()) "Type" else uiState.typeKeywordValue,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.width(120.dp).menuAnchor(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(expanded = kwTypeExpanded, onDismissRequest = { kwTypeExpanded = false }) {
                                listOf("INCOME", "EXPENSE", "TRANSFER").forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(t) },
                                        onClick = { viewModel.updateTypeKeywordValue(t); kwTypeExpanded = false }
                                    )
                                }
                            }
                        }
                        IconButton(onClick = viewModel::addKeyword) {
                            Icon(Icons.Default.Add, contentDescription = "Add Keyword", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    uiState.typeKeywords.forEachIndexed { index, (keyword, type) ->
                        AssistChip(
                            onClick = { viewModel.removeKeyword(index) },
                            label = { Text("$keyword → $type") },
                            trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            // Paste SMS (Auto-detect)
            ManaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Dimensions.Padding.content),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text("Quick Setup: Paste SMS", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Text(
                        "Paste a real SMS from this bank to auto-detect patterns",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = uiState.sampleSms,
                        onValueChange = viewModel::updateSampleSms,
                        label = { Text("Sample SMS") },
                        placeholder = { Text("Paste an SMS from this bank...") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                        maxLines = 6
                    )
                    Button(
                        onClick = viewModel::autoDetectFromSample,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.sampleSms.isNotBlank()
                    ) {
                        Text("Auto-detect Patterns")
                    }
                }
            }

            // Regex Fields
            ManaCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(Dimensions.Padding.content),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text("Regex Patterns", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Text(
                        "Leave blank to skip field. Use named groups for most parsers, but simple capture groups work too.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    RegexField("Amount", uiState.amountRegex, viewModel::updateAmountRegex, "e.g. (\\\\d+,\\\\d+)")
                    RegexField("Balance", uiState.balanceRegex, viewModel::updateBalanceRegex, "e.g. (\\\\d+,\\\\d+)")
                    RegexField("Account", uiState.accountRegex, viewModel::updateAccountRegex, "e.g. (\\\\d{4,})")
                    RegexField("Merchant", uiState.merchantRegex, viewModel::updateMerchantRegex, "e.g. (.+)")
                    RegexField("Reference", uiState.referenceRegex, viewModel::updateReferenceRegex, "e.g. (\\\\d+)")
                }
            }

            uiState.error?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
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
                Text(if (templateId > 0) "Update" else "Save")
            }

            Spacer(modifier = Modifier.height(Spacing.md))
        }
    }
}

@Composable
private fun RegexField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}
