package com.mana.ui.screens.custombanks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mana.data.database.entity.SmsTemplateEntity
import com.mana.data.database.entity.UserBankEntity
import com.mana.data.repository.SmsTemplateRepository
import com.mana.data.repository.UserBankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BankTemplatesUiState(
    val bank: UserBankEntity? = null,
    val templates: List<SmsTemplateEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class BankTemplatesViewModel @Inject constructor(
    private val userBankRepository: UserBankRepository,
    private val smsTemplateRepository: SmsTemplateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BankTemplatesUiState())
    val uiState: StateFlow<BankTemplatesUiState> = _uiState.asStateFlow()

    fun loadBank(bankId: Long) {
        viewModelScope.launch {
            val bank = userBankRepository.getBankById(bankId)
            _uiState.value = _uiState.value.copy(bank = bank)
            smsTemplateRepository.getTemplatesByBankId(bankId).collect { templates ->
                _uiState.value = _uiState.value.copy(templates = templates, isLoading = false)
            }
        }
    }

    fun deleteTemplate(template: SmsTemplateEntity) {
        viewModelScope.launch {
            smsTemplateRepository.delete(template)
        }
    }
}
