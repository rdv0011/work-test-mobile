package io.umain.munchies.android.navigation

import io.umain.munchies.core.ui.IconId
import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.NavigationReducer
import io.umain.munchies.navigation.NavigationEvent
import io.umain.munchies.navigation.NavigationState
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.ScreenEntry
import io.umain.munchies.navigation.TabDefinition
import io.umain.munchies.navigation.TabNavigationState
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Unit tests for ViewModel scope survival through configuration changes.
 *
 * These tests verify the pure logic of the navigation system without
 * requiring Android instrumentation. They test:
 *
 * 1. scopeId stability across navigation operations
 * 2. ScreenEntry immutability and equality
 * 3. Navigation state preservation during config changes
 *
 * These tests run quickly (no emulator needed) and provide regression detection.
 */
class ViewModelScopeSurvivalUnitTest {

    /**
     * Test: scopeId is preserved in ScreenEntry
     *
     * Verifies that a ScreenEntry created with a specific scopeId
     * maintains that scopeId through multiple references and copies.
     */
    @Test
    fun testScreenEntryScopeIdIsStable() {
        // Given
        val route = RestaurantListRoute()
        val scopeId = "scope-${System.currentTimeMillis()}"
        val entry = ScreenEntry(route, scopeId)

        // When
        val copiedEntry = entry.copy()
        val referencedEntry = entry

        // Then
        assertEquals(entry.scopeId, scopeId, "Original scopeId should be preserved")
        assertEquals(copiedEntry.scopeId, scopeId, "Copied entry should have same scopeId")
        assertEquals(referencedEntry.scopeId, scopeId, "Referenced entry should have same scopeId")
        assertEquals(entry, copiedEntry, "Entries with same route and scopeId should be equal")
    }

    /**
     * Test: Different scopeIds create different ScreenEntry instances
     *
     * Verifies that two ScreenEntries with different scopeIds are not equal.
     */
    @Test
    fun testDifferentScopeIdsCreateDifferentEntries() {
        // Given
        val route = RestaurantListRoute()
        val scopeId1 = "scope-1-${System.currentTimeMillis()}"
        val scopeId2 = "scope-2-${System.currentTimeMillis()}"

        val entry1 = ScreenEntry(route, scopeId1)
        val entry2 = ScreenEntry(route, scopeId2)

        // Then
        assertNotEquals(entry1.scopeId, entry2.scopeId, "Different scopeIds should differ")
        assertNotEquals(entry1, entry2, "Entries with different scopeIds should not be equal")
    }

    /**
     * Test: Navigation state preserves stack entries
     *
     * Simulates pushing two screens and verifies that the navigation state
     * contains both entries with their respective scopeIds intact.
     */
    @Test
    fun testNavigationStatePushPreservesScopeIds() {
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
        assertEquals(1, stack.size, "Stack should have one entry after push")

        val pushEntry = stack.last()
        assertEquals("RestaurantList", pushEntry.route.key, "Route key should match")

        val scopeIdAfterPush = pushEntry.scopeId

        // When - simulating a config change by reading the same state again
        val stateAfterConfigChange = stateAfterPush  // No change in state

        // Then
        val stackAfterConfigChange = stateAfterConfigChange.currentStack
        val entryAfterConfigChange = stackAfterConfigChange.last()

        assertEquals(
            scopeIdAfterPush,
            entryAfterConfigChange.scopeId,
            "scopeId should be identical after config change simulation"
        )
    }

    /**
     * Test: Multiple push operations create distinct scopeIds
     *
     * Verifies that each navigation push creates a unique scopeId,
     * so ViewModels don't collide.
     */
    @Test
    fun testMultiplePushesCreateDistinctScopeIds() {
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
        assertEquals(2, stack.size, "Stack should have two entries")

        val scopeId1 = stack[0].scopeId
        val scopeId2 = stack[1].scopeId

        assertNotEquals(scopeId1, scopeId2, "Each push should create unique scopeId")
    }

    /**
     * Test: Popping a screen removes its entry
     *
     * Verifies that NavigationReducer.Pop correctly removes the top entry
     * from the stack, and the remaining entry's scopeId is unchanged.
     */
    @Test
    fun testPopRemovesEntryButPreservesRemaining() {
        // Given
        val initialState = createTestNavigationState()
        val destination1 = Destination.RestaurantList
        val destination2 = Destination.RestaurantList

        val stateAfterTwoPushes = NavigationReducer.reduce(
            NavigationReducer.reduce(initialState, NavigationEvent.Push(destination1)),
            NavigationEvent.Push(destination2)
        )

        val firstEntry = stateAfterTwoPushes.currentStack[0]
        val firstScopeId = firstEntry.scopeId

        // When
        val stateAfterPop = NavigationReducer.reduce(stateAfterTwoPushes, NavigationEvent.Pop)

        // Then
        val remainingStack = stateAfterPop.currentStack
        assertEquals(1, remainingStack.size, "Stack should have one entry after pop")

        val remainingEntry = remainingStack.last()
        assertEquals(
            firstScopeId,
            remainingEntry.scopeId,
            "Remaining entry scopeId should be unchanged after pop"
        )
    }

    /**
     * Test: Tab switching preserves per-tab stacks
     *
     * Verifies that switching tabs doesn't affect the scopeIds in each tab's stack.
     */
    @Test
    fun testTabSwitchPreservesStackScopeIds() {
        // Given
        val initialState = createTestNavigationState()
        val destination = Destination.RestaurantList

        // Push to first tab (restaurants)
        val stateAfterPushRestaurant = NavigationReducer.reduce(
            initialState,
            NavigationEvent.PushInTab(destination)
        )

        val restaurantStack = stateAfterPushRestaurant.tabNavigation.getActiveTabStack()
        val restaurantScopeId = restaurantStack.last().scopeId

        // When - switch to settings tab
        val stateAfterTabSwitch = NavigationReducer.reduce(
            stateAfterPushRestaurant,
            NavigationEvent.SelectTab("settings")
        )

        // Then - restaurant stack should still have same scopeId
        val restaurantStackAfterSwitch = stateAfterTabSwitch.tabNavigation.getTabStack("restaurants")
        assertEquals(
            restaurantScopeId,
            restaurantStackAfterSwitch.last().scopeId,
            "scopeId should be preserved when switching tabs"
        )
    }

    /**
     * Test: Re-selecting the same tab doesn't mutate stack
     *
     * Verifies that selecting the same tab multiple times doesn't change
     * the entries or their scopeIds.
     */
    @Test
    fun testReSelectingTabPreservesStack() {
        // Given
        val initialState = createTestNavigationState()
        val destination = Destination.RestaurantList

        val stateAfterPush = NavigationReducer.reduce(
            initialState,
            NavigationEvent.PushInTab(destination)
        )

        val stack1 = stateAfterPush.tabNavigation.getActiveTabStack()
        val scopeId1 = stack1.last().scopeId

        // When - select the same tab multiple times
        val stateAfterReselect1 = NavigationReducer.reduce(
            stateAfterPush,
            NavigationEvent.SelectTab("restaurants")
        )

        val stateAfterReselect2 = NavigationReducer.reduce(
            stateAfterReselect1,
            NavigationEvent.SelectTab("restaurants")
        )

        // Then
        val stack2 = stateAfterReselect2.tabNavigation.getActiveTabStack()
        val scopeId2 = stack2.last().scopeId

        assertEquals(
            scopeId1,
            scopeId2,
            "Reselecting tab should not change scopeIds"
        )
    }

    /**
     * Test: scopeId is unique across all navigation sessions
     *
     * Verifies that every time we push a screen, it gets a new unique scopeId.
     * This ensures VMs don't leak or collide between sessions.
     */
    @Test
    fun testScopeIdUniquenessAcrossSessions() {
        // Given
        val destination = Destination.RestaurantList
        val scopeIds = mutableSetOf<String>()

        // When - perform multiple pushes
        var state = createTestNavigationState()
        repeat(10) {
            state = NavigationReducer.reduce(state, NavigationEvent.Push(destination))
            scopeIds.add(state.currentStack.last().scopeId)
        }

        // Then - all scopeIds should be unique
        assertEquals(10, scopeIds.size, "All 10 pushes should have unique scopeIds")
    }

    /**
     * Test: ScreenEntry is immutable (data class contract)
     *
     * Verifies that ScreenEntry behaves as an immutable data structure.
     */
    @Test
    fun testScreenEntryImmutability() {
        // Given
        val route = RestaurantListRoute()
        val scopeId = "immutable-test-scope"
        val entry = ScreenEntry(route, scopeId)

        // When - create a copy with different scopeId
        val modifiedEntry = entry.copy(scopeId = "different-scope")

        // Then - original should be unchanged
        assertEquals(scopeId, entry.scopeId, "Original entry should not change")
        assertEquals("different-scope", modifiedEntry.scopeId, "Copy should have new scopeId")
        assertNotEquals(entry, modifiedEntry, "Different entries should not be equal")
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
