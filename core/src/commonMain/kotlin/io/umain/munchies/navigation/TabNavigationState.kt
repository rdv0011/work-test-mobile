package io.umain.munchies.navigation

import io.umain.munchies.core.ui.IconId
import io.umain.munchies.core.ui.TextId
import kotlinx.serialization.Serializable

/**
 * State for tab-based navigation.
 *
 * Each tab maintains its own back stack, allowing users to navigate
 * within a tab and preserve their position when switching away and back.
 */
data class TabNavigationState(
    val tabDefinitions: List<TabDefinition>,
    val activeTabId: String,
    val stacksByTab: Map<String, List<Route>>
) {

    /**
     * Get the current stack for the active tab
     */
    fun getActiveTabStack(): List<Route> {
        return stacksByTab[activeTabId] ?: emptyList()
    }

    /**
     * Get stack for a specific tab
     */
    fun getTabStack(tabId: String): List<Route> {
        return stacksByTab[tabId] ?: emptyList()
    }

    /**
     * Create a new TabNavigationState with updated active tab's stack
     */
    fun updateActiveTabStack(newStack: List<Route>): TabNavigationState {
        return copy(
            stacksByTab = stacksByTab.toMutableMap().apply {
                this[activeTabId] = newStack
            }
        )
    }

    /**
     * Create a new TabNavigationState with updated tab stack
     */
    fun updateTabStack(tabId: String, newStack: List<Route>): TabNavigationState {
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
    val label: TextId,
    val icon: IconId,
    val rootRoute: Route
)
