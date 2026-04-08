package io.umain.munchies.android.navigation

import io.umain.munchies.core.ui.IconId
import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.NavigationReducer
import io.umain.munchies.navigation.NavigationEvent
import io.umain.munchies.navigation.NavigationState
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.TabDefinition
import io.umain.munchies.navigation.TabNavigationState
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for Route navigation stability and state preservation.
 *
 * These tests verify the pure logic of the navigation system without
 * requiring Android instrumentation. They test:
 *
 * 1. Route stability across navigation operations
 * 2. Navigation state preservation during simulated config changes
 *
 * These tests run quickly (no emulator needed) and provide regression detection.
 */
class ViewModelScopeSurvivalUnitTest {

    /**
     * Test: Navigation state preserves stack routes
     *
     * Simulates pushing a screen and verifies that the navigation state
     * contains the route.
     */
    @Test
    fun testNavigationStatePushPreservesRoutes() {
        // Given
        val initialState = createTestNavigationState()
        val destination1 = Destination.RestaurantList

        // When
        val stateAfterPush = NavigationReducer.reduce(
            initialState,
            NavigationEvent.Push(destination1)
        )

        // Then
        val stack = stateAfterPush.currentStack
        assertEquals(1, stack.size, "Stack should have one route after push")

        val route = stack.last()
        assertEquals("RestaurantList", route.key, "Route key should match")
    }

    /**
     * Test: Multiple push operations add to the stack
     */
    @Test
    fun testMultiplePushesAddToStack() {
        // Given
        val initialState = createTestNavigationState()
        val destination1 = Destination.RestaurantList
        val destination2 = Destination.RestaurantList

        // When
        val stateAfterFirstPush = NavigationReducer.reduce(
            initialState,
            NavigationEvent.Push(destination1)
        )

        val stateAfterSecondPush = NavigationReducer.reduce(
            stateAfterFirstPush,
            NavigationEvent.Push(destination2)
        )

        // Then
        val stack = stateAfterSecondPush.currentStack
        assertEquals(2, stack.size, "Stack should have two routes")
        assertTrue(stack[0] is RestaurantListRoute)
        assertTrue(stack[1] is RestaurantListRoute)
    }

    /**
     * Test: Popping a screen removes its route
     */
    @Test
    fun testPopRemovesRoute() {
        // Given
        val initialState = createTestNavigationState()
        val destination1 = Destination.RestaurantList
        val destination2 = Destination.RestaurantList

        val stateAfterTwoPushes = NavigationReducer.reduce(
            NavigationReducer.reduce(initialState, NavigationEvent.Push(destination1)),
            NavigationEvent.Push(destination2)
        )

        val firstRoute = stateAfterTwoPushes.currentStack[0]

        // When
        val stateAfterPop = NavigationReducer.reduce(stateAfterTwoPushes, NavigationEvent.Pop)

        // Then
        val remainingStack = stateAfterPop.currentStack
        assertEquals(1, remainingStack.size, "Stack should have one route after pop")

        val remainingRoute = remainingStack.last()
        assertEquals(
            firstRoute.key,
            remainingRoute.key,
            "Remaining route should be the first one pushed"
        )
    }

    /**
     * Test: Tab switching preserves per-tab stacks
     */
    @Test
    fun testTabSwitchPreservesRoutes() {
        // Given
        val initialState = createTestNavigationState()
        val destination = Destination.RestaurantList

        // Push to first tab (restaurants)
        val stateAfterPushRestaurant = NavigationReducer.reduce(
            initialState,
            NavigationEvent.PushInTab(destination)
        )

        val restaurantRoute = stateAfterPushRestaurant.tabNavigation.getActiveTabStack().last()

        // When - switch to settings tab
        val stateAfterTabSwitch = NavigationReducer.reduce(
            stateAfterPushRestaurant,
            NavigationEvent.SelectTab("settings")
        )

        // Then - restaurant stack should still have same route
        val restaurantStackAfterSwitch = stateAfterTabSwitch.tabNavigation.getTabStack("restaurants")
        assertEquals(
            restaurantRoute.key,
            restaurantStackAfterSwitch.last().key,
            "Route should be preserved when switching tabs"
        )
    }

    // ==================== HELPER FUNCTIONS ====================

    /**
     * Creates a test NavigationState with default tab navigation setup
     */
    private fun createTestNavigationState(): NavigationState {
        val tabDefinitions = listOf(
            TabDefinition(
                id = "restaurants",
                label = "tab_restaurants",
                icon = IconId.Restaurant,
                rootRoute = RestaurantListRoute()
            ),
            TabDefinition(
                id = "settings",
                label = "tab_settings",
                icon = IconId.Settings,
                rootRoute = RestaurantListRoute()  // Use same route for simplicity
            )
        )

        val tabNavState = TabNavigationState(
            tabDefinitions = tabDefinitions,
            activeTabId = "restaurants",
            stacksByTab = mapOf(
                "restaurants" to emptyList(),
                "settings" to emptyList()
            )
        )

        return NavigationState(
            tabNavigation = tabNavState
        )
    }
}
