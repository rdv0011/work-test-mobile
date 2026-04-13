package io.umain.munchies.navigation

/**
 * NavigationEffects: Manages Koin scope lifecycle in response to navigation state changes.
 *
 * Ownership model (per refactoring plan):
 * - AppCoordinator owns scope creation (on push) and scope closure (on pop)
 * - UI layer NEVER creates or closes scopes; it only resolves VMs from already-open scopes
 */
object NavigationEffects {
    /**
     * Closes Koin scopes for routes that have been removed from the navigation state.
     *
     * Called by AppCoordinator after every state reduction so that scope lifetime
     * is always driven by navigation state, not by Composable lifecycle.
     */
    fun handleNavigationSideEffects(previousState: NavigationState, newState: NavigationState) {
        val removedRoutes = previousState.getAllRoutes() - newState.getAllRoutes()
        removedRoutes.forEach { route ->
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

// Expect function for platform Koin scope lookup (no longer used by reducer)
expect fun getKoinScopeOrNull(scopeId: String): io.umain.munchies.core.lifecycle.Closeable?

// Expect function for platform Koin scope creation (no longer used by reducer)
expect fun createKoinScope(scopeId: String, qualifier: String)
