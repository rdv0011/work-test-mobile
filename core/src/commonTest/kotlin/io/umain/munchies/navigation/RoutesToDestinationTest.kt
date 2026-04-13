package io.umain.munchies.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for Route.toDestination() extension added as part of the scope lifecycle
 * refactoring — AppCoordinator uses this to match routes to ScopedRouteHandlers.
 */
class RoutesToDestinationTest {

    // ── Happy-path mappings ──────────────────────────────────────────────────

    @Test
    fun testRestaurantListRouteMapsToCoreDestination() {
        val route = RestaurantListRoute()
        val destination = route.toDestination()
        assertEquals(Destination.RestaurantList, destination)
    }

    @Test
    fun testRestaurantDetailRouteMapsToDestinationWithCorrectId() {
        val route = RestaurantDetailRoute("restaurant-42")
        val destination = route.toDestination()
        assertEquals(Destination.RestaurantDetail("restaurant-42"), destination)
    }

    @Test
    fun testRestaurantDetailRoutePreservesRestaurantId() {
        val id = "special_chars-123_abc"
        val route = RestaurantDetailRoute(id)
        val destination = route.toDestination() as Destination.RestaurantDetail
        assertEquals(id, destination.restaurantId)
    }

    @Test
    fun testSettingsRouteMapsToSettingsDestination() {
        val route = SettingsRoute()
        val destination = route.toDestination()
        assertEquals(Destination.Settings, destination)
    }

    // ── Null for non-screen routes ───────────────────────────────────────────

    @Test
    fun testModalRouteReturnsNull() {
        val modal = FilterModalRoute()
        assertNull(modal.toDestination(), "Modal routes have no Destination — should return null")
    }

    @Test
    fun testUnknownStackRouteReturnsNull() {
        val unknown = object : StackRoute() {
            override val key = "unknown_screen"
        }
        assertNull(
            unknown.toDestination(),
            "Unknown route types should return null rather than throw"
        )
    }

    // ── Stability / idempotence ──────────────────────────────────────────────

    @Test
    fun testToDestinationIsIdempotent() {
        val route = RestaurantDetailRoute("abc")
        assertEquals(route.toDestination(), route.toDestination())
    }

    @Test
    fun testTwoRoutesWithSameIdProduceSameDestination() {
        val a = RestaurantDetailRoute("same-id")
        val b = RestaurantDetailRoute("same-id")
        assertEquals(a.toDestination(), b.toDestination())
    }

    @Test
    fun testTwoRoutesWithDifferentIdsProduceDifferentDestinations() {
        val a = RestaurantDetailRoute("id-1")
        val b = RestaurantDetailRoute("id-2")
        val destA = a.toDestination() as Destination.RestaurantDetail
        val destB = b.toDestination() as Destination.RestaurantDetail
        assertEquals("id-1", destA.restaurantId)
        assertEquals("id-2", destB.restaurantId)
    }
}
