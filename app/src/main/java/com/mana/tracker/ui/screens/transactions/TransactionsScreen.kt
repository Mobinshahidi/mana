package com.mana.tracker.ui.screens.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mana.tracker.R
import com.mana.tracker.core.format.CurrencyFormatter
import com.mana.tracker.ui.viewmodel.TransactionsViewModel

@Composable
fun TransactionsScreen(
    localeTag: String,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val items by viewModel.transactions.collectAsStateWithLifecycle()
    val filters by viewModel.filterState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(stringResource(R.string.transactions_title), style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = filters.query,
            onValueChange = viewModel::updateQuery,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.search_merchant)) }
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(items) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(item.merchant)
                        Text(item.type)
                        Text(CurrencyFormatter.formatIrr(item.amountIrr, localeTag))
                    }
                }
            }
        }
    }
}
