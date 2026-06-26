package com.mana.tracker.ui.screens.custombanks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mana.tracker.data.database.entity.UserBankEntity
import com.mana.tracker.data.repository.UserBankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditBankUiState(
    val name: String = "",
    val senderNumbers: String = "",
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddEditBankViewModel @Inject constructor(
    private val userBankRepository: UserBankRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditBankUiState())
    val uiState: StateFlow<AddEditBankUiState> = _uiState.asStateFlow()

    private var existingId: Long = -1L

    fun loadBank(bankId: Long) {
        if (bankId <= 0) return
        existingId = bankId
        viewModelScope.launch {
            val bank = userBankRepository.getBankById(bankId) ?: return@launch
            _uiState.value = _uiState.value.copy(
                name = bank.name,
                senderNumbers = bank.senderNumbers
            )
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }

    fun updateSenderNumbers(numbers: String) {
        _uiState.value = _uiState.value.copy(senderNumbers = numbers, error = null)
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(error = "Bank name is required")
            return
        }
        if (state.senderNumbers.isBlank()) {
            _uiState.value = state.copy(error = "At least one sender number is required")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            if (existingId > 0) {
                userBankRepository.update(
                    UserBankEntity(id = existingId, name = state.name, senderNumbers = state.senderNumbers)
                )
            } else {
                userBankRepository.insert(
                    UserBankEntity(name = state.name, senderNumbers = state.senderNumbers)
                )
            }
            _uiState.value = _uiState.value.copy(isSaving = false, saved = true)
        }
    }
}
