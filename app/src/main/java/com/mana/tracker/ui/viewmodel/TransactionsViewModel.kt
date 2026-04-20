package com.mana.tracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mana.tracker.data.local.entity.TransactionEntity
import com.mana.tracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class TxFilterState(
    val query: String = "",
    val type: String = "",
    val fromEpoch: Long = 0,
    val toEpoch: Long = Long.MAX_VALUE
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val filters = MutableStateFlow(TxFilterState())
    val filterState: StateFlow<TxFilterState> = filters.asStateFlow()

    val transactions: StateFlow<List<TransactionEntity>> = filters
        .flatMapLatest { filter ->
            transactionRepository.observeFiltered(
                query = filter.query,
                type = filter.type,
                fromEpoch = filter.fromEpoch,
                toEpoch = filter.toEpoch
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateQuery(query: String) {
        filters.value = filters.value.copy(query = query)
    }
}
