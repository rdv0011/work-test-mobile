package io.umain.munchies.navigation

import io.umain.munchies.logging.logInfo
import io.umain.munchies.core.lifecycle.Closeable
/**
 * NavigationEffects: Handles navigation-related side effects, such as closing Koin scopes for removed routes.
 */
object NavigationEffects {
    /**
     * Call after every navigation event. Closes Koin scopes for any ScreenEntries removed from the navigation state.
     * @param previousState The navigation state before the event.
     * @param newState The navigation state after the event.
     */
    fun handleNavigationSideEffects(previousState: NavigationState, newState: NavigationState) {
        val previousEntries = previousState.getAllScreenEntries()
        val newEntries = newState.getAllScreenEntries()
        val removedScopeIds = previousEntries.map { it.scopeId }.toSet() - newEntries.map { it.scopeId }.toSet()
        logInfo("NavigationEffects", "handleNavigationSideEffects: removedScopeIds=$removedScopeIds")
        for (scopeId in removedScopeIds) {
            logInfo("NavigationEffects", "Attempting to close Koin scope for scopeId=$scopeId")
            getKoinScopeOrNull(scopeId)?.close()
        }
    }
}

/**
 * Extension to get all ScreenEntries (tab stacks) from NavigationState.
 */
fun NavigationState.getAllScreenEntries(): List<ScreenEntry> {
    return tabNavigation.stacksByTab.values.flatten()
}

/**
 * Extension to get all routes (tab stacks + modals) from NavigationState.
 * Used for analytics and other consumers that need Route objects.
 */
fun NavigationState.getAllRoutes(): Set<Route> {
    val tabRoutes = tabNavigation.stacksByTab.values.flatten().map { it.route }
    val modalRoutes = modalStack
    return (tabRoutes + modalRoutes).toSet()
}

// Expect function for platform Koin scope lookup
expect fun getKoinScopeOrNull(scopeId: String): Closeable?

// Expect function for platform Koin scope creation
expect fun createKoinScope(scopeId: String, qualifier: String)

