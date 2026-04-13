package io.umain.munchies.navigation

import kotlinx.serialization.Serializable
import io.umain.munchies.core.ui.IconId
import io.umain.munchies.core.localization.StringKey
import io.umain.munchies.core.util.currentTimeMillis

/**
 * Serializable snapshot of NavigationState for persistence.
 * Mirrors the structure of NavigationState but with all fields serializable.
 */
@Serializable
data class NavigationStateSnapshot(
    val tabNavigation: TabNavigationStateSnapshot,
    val modalStack: List<ModalRoute> = emptyList(),
    val originDeepLink: String? = null,
    val restoredFromCrash: Boolean = false,
    val restorationTimestamp: Long = 0L
)

/**
 * Serializable snapshot of TabNavigationState.
 */
@Serializable
data class TabNavigationStateSnapshot(
    val tabDefinitions: List<TabDefinitionSnapshot>,
    val activeTabId: String,
    val stacksByTab: Map<String, List<Route>>,
    val navigationDirection: NavigationDirection = NavigationDirection.Forward
)

/**
 * Serializable snapshot of TabDefinition.
 */
@Serializable
data class TabDefinitionSnapshot(
    val id: String,
    val label: StringKey,
    val icon: IconId,
    val rootRoute: Route
)

/**
 * Convert NavigationState to a serializable snapshot.
 * 
 * @param restoredFromCrash Whether this state was restored from a crash
 * @return Serializable snapshot of the current navigation state
 */
fun NavigationState.toSnapshot(restoredFromCrash: Boolean = false): NavigationStateSnapshot {
    return NavigationStateSnapshot(
        tabNavigation = tabNavigation.toSnapshot(),
        modalStack = modalStack,
        originDeepLink = originDeepLink,
        restoredFromCrash = restoredFromCrash,
        restorationTimestamp = currentTimeMillis()
    )
}

/**
 * Convert TabNavigationState to a serializable snapshot.
 */
fun TabNavigationState.toSnapshot(): TabNavigationStateSnapshot {
    return TabNavigationStateSnapshot(
        tabDefinitions = tabDefinitions.map { it.toSnapshot() },
        activeTabId = activeTabId,
        stacksByTab = stacksByTab,
        navigationDirection = navigationDirection
    )
}

/**
 * Convert TabDefinition to a serializable snapshot.
 */
fun TabDefinition.toSnapshot(): TabDefinitionSnapshot {
    return TabDefinitionSnapshot(
        id = id,
        label = label,
        icon = icon,
        rootRoute = rootRoute
    )
}

/**
 * Restore NavigationState from a serializable snapshot.
 * 
 * @return Restored NavigationState from snapshot
 */
fun NavigationStateSnapshot.toNavigationState(): NavigationState {
    return NavigationState(
        tabNavigation = tabNavigation.toNavigationState(),
        modalStack = modalStack,
        originDeepLink = originDeepLink
    )
}

/**
 * Restore TabNavigationState from a serializable snapshot.
 */
fun TabNavigationStateSnapshot.toNavigationState(): TabNavigationState {
    return TabNavigationState(
        tabDefinitions = tabDefinitions.map { it.toTabDefinition() },
        activeTabId = activeTabId,
        stacksByTab = stacksByTab,
        navigationDirection = navigationDirection
    )
}

/**
 * Restore TabDefinition from a serializable snapshot.
 */
fun TabDefinitionSnapshot.toTabDefinition(): TabDefinition {
    return TabDefinition(
        id = id,
        label = label,
        icon = icon,
        rootRoute = rootRoute
    )
}
