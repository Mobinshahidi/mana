package com.mana.tracker.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.mana.tracker.core.format.CurrencyFormatter
import com.mana.tracker.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    localeTag: String,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = stringResource(R.string.balance))
                    Text(
                        text = CurrencyFormatter.formatIrr(state.balanceIrr, localeTag),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        }
        item {
            Text(stringResource(R.string.recent_transactions), style = MaterialTheme.typography.titleMedium)
        }
        items(state.recent.take(8)) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(item.merchant)
                        Text(item.type, style = MaterialTheme.typography.labelMedium)
                    }
                    Text(CurrencyFormatter.formatIrr(item.amountIrr, localeTag))
                }
            }
        }
    }
}
