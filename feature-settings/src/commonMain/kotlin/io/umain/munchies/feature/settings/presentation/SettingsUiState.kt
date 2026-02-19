package io.umain.munchies.feature.settings.presentation

import io.umain.munchies.core.state.ViewState

data class SettingsUiState(
    val darkModeEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val appVersion: String = "1.0.0"
) : ViewState
