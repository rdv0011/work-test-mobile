package io.umain.munchies.navigation.persistence

import io.umain.munchies.navigation.NavigationState
import io.umain.munchies.navigation.NavigationStateSnapshot
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.SettingsRoute
import io.umain.munchies.navigation.TabDefinition
import io.umain.munchies.navigation.TabNavigationState
import io.umain.munchies.navigation.toNavigationState
import io.umain.munchies.core.localization.StringResources
import io.umain.munchies.core.ui.IconId
import io.umain.munchies.logging.logError
import io.umain.munchies.logging.logInfo

class NavigationStateRestorer(
    private val persistenceStore: NavigationPersistenceStore
) {

    suspend fun restoreNavigationState(): NavigationState {
        return try {
            val result = persistenceStore.loadNavigationState()
            if (result.isSuccess) {
                val snapshot = result.getOrNull()
                when {
                    snapshot == null -> {
                        logInfo("NavigationStateRestorer", "No persisted state found, using default")
                        createDefaultNavigationState()
                    }
                    isValidSnapshot(snapshot) -> {
                        logInfo("NavigationStateRestorer", "Restored navigation state from persistence")
                        snapshot.toNavigationState()
                    }
                    else -> {
                        logInfo("NavigationStateRestorer", "Persisted snapshot invalid, using default")
                        createDefaultNavigationState()
                    }
                }
            } else {
                logError(
                    "NavigationStateRestorer",
                    "Failed to load persisted navigation state: ${result.exceptionOrNull()?.message}"
                )
                createDefaultNavigationState()
            }
        } catch (e: Exception) {
            logError("NavigationStateRestorer", "Unexpected error restoring navigation state: ${e.message}")
            createDefaultNavigationState()
        }
    }

    private fun isValidSnapshot(snapshot: NavigationStateSnapshot): Boolean {
        val tabNav = snapshot.tabNavigation
        return tabNav.tabDefinitions.isNotEmpty() &&
            tabNav.tabDefinitions.any { it.id == tabNav.activeTabId } &&
            tabNav.stacksByTab.values.all { it.isNotEmpty() }
    }
}

fun createDefaultNavigationState(): NavigationState {
    val restaurantsTab = TabDefinition(
        id = "restaurants",
        label = StringResources.tab_restaurants,
        icon = IconId.Restaurant,
        rootRoute = RestaurantListRoute()
    )
    val settingsTab = TabDefinition(
        id = "settings",
        label = StringResources.tab_settings,
        icon = IconId.Settings,
        rootRoute = SettingsRoute()
    )
    val tabNav = TabNavigationState(
        tabDefinitions = listOf(restaurantsTab, settingsTab),
        activeTabId = "restaurants",
        stacksByTab = mapOf(
            "restaurants" to listOf(RestaurantListRoute()),
            "settings" to listOf(SettingsRoute())
        )
    )
    return NavigationState(tabNavigation = tabNav)
}
