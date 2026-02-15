package io.umain.munchies.navigation

/**
 * Handles the routing logic for a specific route.
 *
 * Each route type should have a corresponding handler that knows how to:
 * - Convert destinations to routes
 * - Check if it can handle a given destination
 * - Convert routes to platform-specific representations
 *
 * This interface allows features to own their route handling logic without
 * modifying the app layer.
 */
interface RouteHandler {
    /**
     * The template route this handler manages.
     * For parameterized routes (e.g., RestaurantDetail with ID),
     * this is the template representation.
     */
    val route: Route

    /**
     * Convert a Route to platform-specific representation.
     *
     * iOS: Used for identifying which view to show
     * Android: Route string for NavController.navigate()
     *
     * @return String representation of the route
     */
    fun toRouteString(): String

    /**
     * Check if this handler can handle the given destination.
     *
     * @param destination The destination to check
     * @return true if this handler can convert the destination to a route
     */
    fun canHandle(destination: Destination): Boolean

    /**
     * Convert a destination to a Route instance.
     *
     * @param destination The destination to convert
     * @return Route instance if this handler can handle it, null otherwise
     */
    fun destinationToRoute(destination: Destination): Route?
}
