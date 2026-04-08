package io.umain.munchies.navigation

/**
 * NavigationEffects: No-op for platform layer after refactoring.
 * Scoping is now managed in the UI layer.
 */
object NavigationEffects {
    /**
     * Handled in UI layer via DisposableEffect.
     */
    fun handleNavigationSideEffects(previousState: NavigationState, newState: NavigationState) {
        // Scopes are now managed by Composable lifecycle in the platform layer.
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
