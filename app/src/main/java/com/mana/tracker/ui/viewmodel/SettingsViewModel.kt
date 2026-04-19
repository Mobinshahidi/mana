package com.mana.tracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mana.tracker.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val localeTag: StateFlow<String> = settingsRepository.observeLocale()
        .map { it?.value ?: "fa" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "fa")

    fun setLocale(tag: String) {
        viewModelScope.launch {
            settingsRepository.setLocale(tag)
        }
    }
}
