package io.umain.munchies.core.lifecycle

import org.koin.core.scope.Scope

/**
 * Single ownership point for route-scoped state.
 *
 * Every active route has exactly one RouteLifetime. The Route Registry creates and retains
 * RouteLifetime instances. Features must NOT retain RouteLifetime directly.
 *
 * Design principle:
 * - If two systems both retain a scope reference, neither truly owns its lifetime.
 * - RouteRegistry owns RouteLifetime.
 * - RouteLifetime owns the Scope.
 * - Features create scopes but do not own them.
 *
 * This ensures deterministic cleanup: scope closes only when RouteRegistry removes the RouteLifetime.
 */
class RouteLifetime(
    private val routeId: String,
    val scope: Scope
) {
    /**
     * Close the scope and release all resources.
     * Called only by RouteRegistry when route becomes inactive.
     */
    fun close() {
        scope.close()
    }

    override fun toString() = "RouteLifetime($routeId)"
}
