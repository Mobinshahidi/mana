package com.mana.tracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mana.tracker.data.local.entity.TransactionEntity
import com.mana.tracker.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val balanceIrr: Long = 0,
    val recent: List<TransactionEntity> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    transactionRepository: TransactionRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = transactionRepository.observeRecent()
        .map { recent ->
            val balance = recent.sumOf {
                if (it.type == "CREDIT") it.amountIrr else -it.amountIrr
            }
            HomeUiState(balanceIrr = balance, recent = recent)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
