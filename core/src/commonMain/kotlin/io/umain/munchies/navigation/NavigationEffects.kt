package io.umain.munchies.navigation

import io.umain.munchies.logging.logInfo
import io.umain.munchies.core.lifecycle.Closeable
/**
 * NavigationEffects: Handles navigation-related side effects, such as closing Koin scopes for removed routes.
 */
object NavigationEffects {
    /**
     * Call after every navigation event. Closes Koin scopes for any routes that were removed from the navigation state.
     * @param previousState The navigation state before the event.
     * @param newState The navigation state after the event.
     */
    fun handleNavigationSideEffects(previousState: NavigationState, newState: NavigationState) {
        val removedRoutes = previousState.getAllRoutes() - newState.getAllRoutes()
        logInfo("NavigationEffects", "handleNavigationSideEffects: removedRoutes=${removedRoutes.map { it.key }}")
        for (route in removedRoutes) {
            logInfo("NavigationEffects", "Attempting to close Koin scope for route.key=${route.key}")
            getKoinScopeOrNull(route.key)?.close()
        }
    }
}

/**
 * Extension to get all routes (tab stacks + modals) from NavigationState.
 */
fun NavigationState.getAllRoutes(): Set<Route> {
    val tabRoutes = tabNavigation.stacksByTab.values.flatten()
    val modalRoutes = modalStack
    return (tabRoutes + modalRoutes).toSet()
}

// Expect function for platform Koin scope lookup
expect fun getKoinScopeOrNull(scopeId: String): Closeable?
