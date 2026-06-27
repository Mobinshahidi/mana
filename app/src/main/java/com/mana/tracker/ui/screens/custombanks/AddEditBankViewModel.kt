package com.mana.tracker.ui.screens.custombanks

import android.content.Context
import android.provider.Telephony
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mana.tracker.data.database.entity.UserBankEntity
import com.mana.tracker.data.repository.UserBankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AddEditBankUiState(
    val name: String = "",
    val senderNumbers: String = "",
    val detectedSenders: List<String> = emptyList(),
    val isDetecting: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddEditBankViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
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

    fun selectSender(sender: String) {
        val current = _uiState.value.senderNumbers
        val senders = current.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableList()
        if (sender !in senders) {
            senders.add(sender)
        }
        _uiState.value = _uiState.value.copy(senderNumbers = senders.joinToString(","))
    }

    fun detectSenders() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDetecting = true, error = null)
            val senders = withContext(Dispatchers.IO) {
                val senderSet = mutableSetOf<String>()
                try {
                    val cursor = context.contentResolver.query(
                        Telephony.Sms.CONTENT_URI,
                        arrayOf(Telephony.Sms.ADDRESS),
                        "${Telephony.Sms.TYPE} = ?",
                        arrayOf(Telephony.Sms.MESSAGE_TYPE_INBOX.toString()),
                        "${Telephony.Sms.DATE} DESC"
                    )
                    cursor?.use {
                        val addressIndex = it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                        while (it.moveToNext()) {
                            val sender = it.getString(addressIndex) ?: ""
                            if (sender.isNotBlank()) senderSet.add(sender)
                        }
                    }
                } catch (_: Exception) {}
                senderSet.toList().sorted()
            }
            _uiState.value = _uiState.value.copy(
                detectedSenders = senders,
                isDetecting = false
            )
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(error = "Bank name is required")
            return
        }
        if (state.senderNumbers.isBlank()) {
            _uiState.value = state.copy(error = "At least one sender ID is required")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            if (existingId > 0) {
                val existing = userBankRepository.getBankById(existingId)
                if (existing != null) {
                    userBankRepository.update(existing.copy(
                        name = state.name,
                        senderNumbers = state.senderNumbers,
                        updatedAt = java.time.LocalDateTime.now()
                    ))
                } else {
                    _uiState.value = state.copy(isSaving = false, error = "Bank not found")
                    return@launch
                }
            } else {
                userBankRepository.insert(
                    UserBankEntity(name = state.name, senderNumbers = state.senderNumbers)
                )
            }
            _uiState.value = _uiState.value.copy(isSaving = false, saved = true)
        }
    }
}
