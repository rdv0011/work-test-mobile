package io.umain.munchies.navigation
/**
 * Feature-provided navigation interface.
 *
 * Each feature that has routes should implement this interface to declare
 * all routes it provides.
 */
interface RouteProvider {
    /**
     * Return all route handlers this feature provides.
     */
    fun getRoutes(): List<RouteHandler>
}
