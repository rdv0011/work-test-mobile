package io.umain.munchies.feature.settings.presentation

import io.umain.munchies.core.lifecycle.KmpViewModel
import io.umain.munchies.core.state.ViewModelState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel : KmpViewModel(), ViewModelState<SettingsUiState> {
    private val _stateFlow = MutableStateFlow<SettingsUiState>(SettingsUiState())
    override val stateFlow: StateFlow<SettingsUiState> = _stateFlow

    fun toggleDarkMode() {
        _stateFlow.value = _stateFlow.value.copy(
            darkModeEnabled = !_stateFlow.value.darkModeEnabled
        )
    }

    fun toggleNotifications() {
        _stateFlow.value = _stateFlow.value.copy(
            notificationsEnabled = !_stateFlow.value.notificationsEnabled
        )
    }
}
