package com.toyota.demo.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.toyota.demo.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = settingsRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val showInferenceStats: StateFlow<Boolean> = settingsRepository.showInferenceStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isTestFeature1Enabled: StateFlow<Boolean> = settingsRepository.isTestFeature1Enabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(isDark)
        }
    }

    fun setShowInferenceStats(show: Boolean) {
        viewModelScope.launch {
            settingsRepository.setShowInferenceStats(show)
        }
    }

    fun setTestFeature1Enabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setTestFeature1Enabled(enabled)
        }
    }
}
