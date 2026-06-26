package com.mana.ui.screens.custombanks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mana.data.database.entity.SmsTemplateEntity
import com.mana.data.repository.SmsTemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

data class AddEditTemplateUiState(
    val name: String = "",
    val transactionType: String? = null,
    val typeKeywordKey: String = "",
    val typeKeywordValue: String = "",
    val typeKeywords: List<Pair<String, String>> = emptyList(),
    val amountRegex: String = "",
    val balanceRegex: String = "",
    val accountRegex: String = "",
    val merchantRegex: String = "",
    val referenceRegex: String = "",
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AddEditTemplateViewModel @Inject constructor(
    private val smsTemplateRepository: SmsTemplateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditTemplateUiState())
    val uiState: StateFlow<AddEditTemplateUiState> = _uiState.asStateFlow()

    private var bankId: Long = -1L
    private var existingId: Long = -1L

    fun init(bankId: Long, templateId: Long) {
        this.bankId = bankId
        this.existingId = templateId
        if (templateId > 0) {
            viewModelScope.launch {
                val t = smsTemplateRepository.getTemplateById(templateId) ?: return@launch
                _uiState.value = _uiState.value.copy(
                    name = t.name,
                    transactionType = t.transactionType,
                    amountRegex = t.amountRegex ?: "",
                    balanceRegex = t.balanceRegex ?: "",
                    accountRegex = t.accountRegex ?: "",
                    merchantRegex = t.merchantRegex ?: "",
                    referenceRegex = t.referenceRegex ?: "",
                    typeKeywords = parseKeywords(t.typeKeywords)
                )
            }
        }
    }

    fun updateName(v: String) { _uiState.value = _uiState.value.copy(name = v, error = null) }
    fun updateTransactionType(v: String?) { _uiState.value = _uiState.value.copy(transactionType = v, error = null) }
    fun updateAmountRegex(v: String) { _uiState.value = _uiState.value.copy(amountRegex = v, error = null) }
    fun updateBalanceRegex(v: String) { _uiState.value = _uiState.value.copy(balanceRegex = v, error = null) }
    fun updateAccountRegex(v: String) { _uiState.value = _uiState.value.copy(accountRegex = v, error = null) }
    fun updateMerchantRegex(v: String) { _uiState.value = _uiState.value.copy(merchantRegex = v, error = null) }
    fun updateReferenceRegex(v: String) { _uiState.value = _uiState.value.copy(referenceRegex = v, error = null) }
    fun updateTypeKeywordKey(v: String) { _uiState.value = _uiState.value.copy(typeKeywordKey = v) }
    fun updateTypeKeywordValue(v: String) { _uiState.value = _uiState.value.copy(typeKeywordValue = v) }

    fun addKeyword() {
        val s = _uiState.value
        if (s.typeKeywordKey.isNotBlank() && s.typeKeywordValue.isNotBlank()) {
            _uiState.value = s.copy(
                typeKeywords = s.typeKeywords + (s.typeKeywordKey to s.typeKeywordValue),
                typeKeywordKey = "", typeKeywordValue = ""
            )
        }
    }

    fun removeKeyword(index: Int) {
        val list = _uiState.value.typeKeywords.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            _uiState.value = _uiState.value.copy(typeKeywords = list)
        }
    }

    fun save() {
        val s = _uiState.value
        if (s.name.isBlank()) {
            _uiState.value = s.copy(error = "Template name is required")
            return
        }
        viewModelScope.launch {
            _uiState.value = s.copy(isSaving = true)
            val typeKeywordsJson = if (s.typeKeywords.isEmpty()) null else {
                JSONArray(s.typeKeywords.map { (k, v) ->
                    JSONObject().apply { put("keyword", k); put("type", v) }
                }).toString()
            }
            val entity = SmsTemplateEntity(
                id = if (existingId > 0) existingId else 0,
                bankId = bankId,
                name = s.name,
                transactionType = s.transactionType,
                typeKeywords = typeKeywordsJson,
                amountRegex = s.amountRegex.ifBlank { null },
                balanceRegex = s.balanceRegex.ifBlank { null },
                accountRegex = s.accountRegex.ifBlank { null },
                merchantRegex = s.merchantRegex.ifBlank { null },
                referenceRegex = s.referenceRegex.ifBlank { null }
            )
            if (existingId > 0) smsTemplateRepository.update(entity)
            else smsTemplateRepository.insert(entity)
            _uiState.value = _uiState.value.copy(isSaving = false, saved = true)
        }
    }

    private fun parseKeywords(json: String?): List<Pair<String, String>> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                obj.getString("keyword") to obj.getString("type")
            }
        } catch (_: Exception) { emptyList() }
    }
}
