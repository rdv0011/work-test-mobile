package io.umain.munchies.feature.settings.navigation

import io.umain.munchies.core.lifecycle.KmpViewModel
import io.umain.munchies.core.navigation.NavigationDispatcher
import io.umain.munchies.navigation.DeepLinkConstants

/**
 * Settings feature-scoped navigation ViewModel.
 * Owns all navigation actions specific to the settings feature.
 */
class SettingsNavigationViewModel(
    private val dispatcher: NavigationDispatcher
) : KmpViewModel() {
    
    fun showSettings() {
        dispatcher.selectTab(tabId = DeepLinkConstants.TAB_ID_SETTINGS)
    }
}
