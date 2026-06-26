package com.mana.ui.screens.custombanks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mana.data.database.entity.UserBankEntity
import com.mana.data.repository.UserBankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomBanksViewModel @Inject constructor(
    private val userBankRepository: UserBankRepository
) : ViewModel() {

    val banks: StateFlow<List<UserBankEntity>> = userBankRepository.getAllBanks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteBank(bank: UserBankEntity) {
        viewModelScope.launch {
            userBankRepository.delete(bank)
        }
    }
}
