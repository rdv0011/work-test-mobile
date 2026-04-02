package io.umain.munchies.navigation

import io.umain.munchies.core.ui.IconId
import io.umain.munchies.core.localization.StringKey

/**
 * State for tab-based navigation.
 *
 * Each tab maintains its own back stack, allowing users to navigate
 * within a tab and preserve their position when switching away and back.
 */
enum class NavigationDirection {
    Forward,
    Back,
    TabSwitch
}

data class ScreenEntry(
    val route: Route,
    val scopeId: String
)

data class TabNavigationState(
    val tabDefinitions: List<TabDefinition>,
    val activeTabId: String,
    val stacksByTab: Map<String, List<ScreenEntry>>,
    val navigationDirection: NavigationDirection = NavigationDirection.Forward
) {

    /**
     * Get the current stack for the active tab
     */
    fun getActiveTabStack(): List<ScreenEntry> {
        return stacksByTab[activeTabId] ?: emptyList()
    }

    /**
     * Get stack for a specific tab
     */
    fun getTabStack(tabId: String): List<ScreenEntry> {
        return stacksByTab[tabId] ?: emptyList()
    }

    /**
     * Create a new TabNavigationState with updated active tab's stack
     */
    fun updateActiveTabStack(newStack: List<ScreenEntry>): TabNavigationState {
        return copy(
            stacksByTab = stacksByTab.toMutableMap().apply {
                this[activeTabId] = newStack
            }
        )
    }

    /**
     * Create a new TabNavigationState with updated tab stack
     */
    fun updateTabStack(tabId: String, newStack: List<ScreenEntry>): TabNavigationState {
        return copy(
            stacksByTab = stacksByTab.toMutableMap().apply {
                this[tabId] = newStack
            }
        )
    }
}

/**
 * Definition of a tab in tab-based navigation
 */
data class TabDefinition(
    val id: String,
    val label: StringKey,
    val icon: IconId,
    val rootRoute: Route
)
