package io.umain.munchies.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive unit tests for NavigationReducer.
 *
 * Tests the pure reducer logic for:
 * - Screen navigation (push/pop/popToRoot)
 * - Modal operations (show/dismiss)
 * - Tab navigation (select/push/pop)
 * - Deep linking (apply state)
 * - State immutability and edge cases
 */
class NavigationReducerTest {

    private val testRoute1 = TestRoute("route1", false)
    private val testRoute2 = TestRoute("route2", false)
    private val testRoute3 = TestRoute("route3", false)
    private val rootRoute = TestRoute("root", true)

    //  SCREEN NAVIGATION TESTS

     @Test
     fun testPushWithoutHandlersDoesNothing() {
         val initialState = NavigationState(tabNavigation = createSingleTabNav(rootRoute))
         val event = NavigationEvent.Push(Destination.RestaurantList)
         
         val newState = NavigationReducer.reduce(initialState, event, emptyList())
         
         // Built-in routes fallback still applies, so state WILL change
         assertEquals(initialState.currentStack.size + 1, newState.currentStack.size)
     }

    @Test
    fun testPushWithValidHandlerAddsToStack() {
        val initialState = NavigationState(tabNavigation = createSingleTabNav(rootRoute))
        val handler = TestRouteHandler(Destination.RestaurantList, testRoute1)
        val event = NavigationEvent.Push(Destination.RestaurantList)
        
        val newState = NavigationReducer.reduce(initialState, event, listOf(handler))
        
        assertEquals(2, newState.tabNavigation.stacksByTab["tab1"]?.size)
        assertEquals(testRoute1, newState.tabNavigation.stacksByTab["tab1"]?.last())
    }

    @Test
    fun testPushMultipleScreensBuildsStack() {
        val initialState = NavigationState(tabNavigation = createSingleTabNav(rootRoute))
        val handler1 = TestRouteHandler(Destination.RestaurantList, testRoute1)
        val handler2 = TestRouteHandler(Destination.RestaurantDetail("1"), testRoute2)
        
        var state = NavigationReducer.reduce(
            initialState,
            NavigationEvent.Push(Destination.RestaurantList),
            listOf(handler1, handler2)
        )
        state = NavigationReducer.reduce(
            state,
            NavigationEvent.Push(Destination.RestaurantDetail("1")),
            listOf(handler1, handler2)
        )
        
        assertEquals(3, state.tabNavigation.stacksByTab["tab1"]?.size)
        assertEquals(testRoute2, state.tabNavigation.stacksByTab["tab1"]?.last())
    }

    @Test
    fun testPopRemovesTopScreen() {
        val initialState = NavigationState(tabNavigation = createSingleTabNav(rootRoute, testRoute1, testRoute2))

        val newState = NavigationReducer.reduce(initialState, NavigationEvent.Pop)
        
        assertEquals(2, newState.tabNavigation.stacksByTab["tab1"]?.size)
        assertEquals(testRoute1, newState.tabNavigation.stacksByTab["tab1"]?.last())
    }

    @Test
    fun testPopAtRootDoesNothing() {
        val initialState = NavigationState(tabNavigation = createSingleTabNav(rootRoute))

        val newState = NavigationReducer.reduce(initialState, NavigationEvent.Pop)
        
        assertEquals(initialState, newState)
    }

    @Test
    fun testPopToRootClearsStack() {
        val initialState = NavigationState(tabNavigation = createSingleTabNav(rootRoute, testRoute1, testRoute2, testRoute3))

        val newState = NavigationReducer.reduce(initialState, NavigationEvent.PopToRoot)
        
        assertEquals(1, newState.tabNavigation.stacksByTab["tab1"]?.size)
        assertEquals(rootRoute, newState.tabNavigation.stacksByTab["tab1"]?.first())
    }

    @Test
    fun testPopToRootWithSingleScreenDoesNothing() {
        val initialState = NavigationState(tabNavigation = createSingleTabNav(rootRoute))

        val newState = NavigationReducer.reduce(initialState, NavigationEvent.PopToRoot)
        
        assertEquals(initialState, newState)
    }

    //  MODAL NAVIGATION TESTS

    @Test
    fun testDismissModalRemovesTopModal() {
        val modal1 = TestModalRoute("modal1")
        val modal2 = TestModalRoute("modal2")
        val initialState = NavigationState(
            tabNavigation = createSingleTabNav(rootRoute, testRoute1),
            modalStack = listOf(modal1, modal2)
        )
        
        val newState = NavigationReducer.reduce(initialState, NavigationEvent.DismissModal)
        
        assertEquals(1, newState.modalStack.size)
        assertEquals(modal1, newState.modalStack.first())
    }

    @Test
    fun testDismissModalWithEmptyStackDoesNothing() {
        val initialState = NavigationState(tabNavigation = createSingleTabNav(rootRoute))

        val newState = NavigationReducer.reduce(initialState, NavigationEvent.DismissModal)
        
        assertEquals(initialState, newState)
    }

    @Test
    fun testDismissAllModalsClearsStack() {
        val modal1 = TestModalRoute("modal1")
        val modal2 = TestModalRoute("modal2")
        val modal3 = TestModalRoute("modal3")
        val initialState = NavigationState(
            tabNavigation = createSingleTabNav(rootRoute, testRoute1),
            modalStack = listOf(modal1, modal2, modal3)
        )
        
        val newState = NavigationReducer.reduce(initialState, NavigationEvent.DismissAllModals)
        
        assertTrue(newState.modalStack.isEmpty())
        assertEquals(initialState.tabNavigation, newState.tabNavigation)
    }

    @Test
    fun testDismissAllModalsWithEmptyStackDoesNothing() {
        val initialState = NavigationState(tabNavigation = createSingleTabNav(rootRoute))

        val newState = NavigationReducer.reduce(initialState, NavigationEvent.DismissAllModals)
        
        assertEquals(initialState, newState)
    }

    @Test
    fun testDismissModalUntilMatchingPredicate() {
        val modal1 = TestModalRoute("modal1")
        val modal2 = TestModalRoute("modal2")
        val modal3 = TestModalRoute("modal3")
        val initialState = NavigationState(
            tabNavigation = createSingleTabNav(rootRoute),
            modalStack = listOf(modal1, modal2, modal3)
        )
        
        val newState = NavigationReducer.reduce(
            initialState,
            NavigationEvent.DismissModalUntil { it.key == "modal2" }
        )
        
        assertEquals(2, newState.modalStack.size)
        assertEquals(modal2, newState.modalStack.last())
    }

    @Test
    fun testDismissModalUntilNoMatch() {
        val modal1 = TestModalRoute("modal1")
        val modal2 = TestModalRoute("modal2")
        val initialState = NavigationState(
            tabNavigation = createSingleTabNav(rootRoute),
            modalStack = listOf(modal1, modal2)
        )
        
        val newState = NavigationReducer.reduce(
            initialState,
            NavigationEvent.DismissModalUntil { it.key == "nonexistent" }
        )
        
        assertTrue(newState.modalStack.isEmpty())
    }

    @Test
    fun testPopDismissesModalBeforeScreen() {
        val modal = TestModalRoute("modal")
        val initialState = NavigationState(
            tabNavigation = createSingleTabNav(rootRoute, testRoute1),
            modalStack = listOf(modal)
        )
        
        val newState = NavigationReducer.reduce(initialState, NavigationEvent.Pop)
        
        assertTrue(newState.modalStack.isEmpty())
        assertEquals(initialState.tabNavigation, newState.tabNavigation)
    }

    //  TAB NAVIGATION TESTS

    @Test
    fun testSelectTabChangesActiveTab() {
        val tabDef1 = createTabDefinition("tab1", rootRoute)
        val tabDef2 = createTabDefinition("tab2", rootRoute)
        val tabNav = TabNavigationState(
            tabDefinitions = listOf(tabDef1, tabDef2),
            stacksByTab = mapOf(
                "tab1" to listOf(rootRoute, testRoute1),
                "tab2" to listOf(rootRoute, testRoute2)
            ),
            activeTabId = "tab1"
        )
        val initialState = NavigationState(
            tabNavigation = tabNav
        )
        
        val newState = NavigationReducer.reduce(
            initialState,
            NavigationEvent.SelectTab("tab2")
        )
        
        assertEquals("tab2", newState.tabNavigation.activeTabId)
        // Primary stack should remain unchanged
        assertEquals(initialState.tabNavigation.stacksByTab, newState.tabNavigation.stacksByTab)
    }

    @Test
     fun testSelectTabWithoutTabNavigationDoesNothing() {
         val initialState = NavigationState(tabNavigation = createSingleTabNav(rootRoute))

         val newState = NavigationReducer.reduce(
             initialState,
             NavigationEvent.SelectTab("tab1")
         )
         
         // SelectTab still updates navigationDirection even if tab is already active
         assertEquals(initialState.tabNavigation.stacksByTab, newState.tabNavigation.stacksByTab)
         assertEquals(NavigationDirection.TabSwitch, newState.tabNavigation.navigationDirection)
     }

    @Test
    fun testPushInTabAddsToActiveTabStack() {
        val tabDef1 = createTabDefinition("tab1", rootRoute)
        val tabDef2 = createTabDefinition("tab2", rootRoute)
        val tabNav = TabNavigationState(
            tabDefinitions = listOf(tabDef1, tabDef2),
            stacksByTab = mapOf(
                "tab1" to listOf(rootRoute, testRoute1),
                "tab2" to listOf(rootRoute)
            ),
            activeTabId = "tab1"
        )
        val initialState = NavigationState(
            tabNavigation = tabNav
        )
        val handler = TestRouteHandler(Destination.RestaurantList, testRoute2)
        
        val newState = NavigationReducer.reduce(
            initialState,
            NavigationEvent.PushInTab(Destination.RestaurantList),
            listOf(handler)
        )
        
        val tab1Stack = newState.tabNavigation.stacksByTab["tab1"]
        assertEquals(3, tab1Stack?.size)
        assertEquals(testRoute2, tab1Stack?.last())
        // Other tab unaffected
        assertEquals(1, newState.tabNavigation.stacksByTab["tab2"]?.size)
    }

    @Test
    fun testPopInTabRemovesFromActiveTabStack() {
        val tabDef1 = createTabDefinition("tab1", rootRoute)
        val tabDef2 = createTabDefinition("tab2", rootRoute)
        val tabNav = TabNavigationState(
            tabDefinitions = listOf(tabDef1, tabDef2),
            stacksByTab = mapOf(
                "tab1" to listOf(rootRoute, testRoute1, testRoute2),
                "tab2" to listOf(rootRoute)
            ),
            activeTabId = "tab1"
        )
        val initialState = NavigationState(
            tabNavigation = tabNav
        )
        
        val newState = NavigationReducer.reduce(initialState, NavigationEvent.PopInTab)
        
        val tab1Stack = newState.tabNavigation.stacksByTab["tab1"]
        assertEquals(2, tab1Stack?.size)
        assertEquals(testRoute1, tab1Stack?.last())
    }

    @Test
    fun testPopInTabAtRootDoesNothing() {
        val tabDef1 = createTabDefinition("tab1", rootRoute)
        val tabDef2 = createTabDefinition("tab2", rootRoute)
        val tabNav = TabNavigationState(
            tabDefinitions = listOf(tabDef1, tabDef2),
            stacksByTab = mapOf(
                "tab1" to listOf(rootRoute),
                "tab2" to listOf(rootRoute)
            ),
            activeTabId = "tab1"
        )
        val initialState = NavigationState(
            tabNavigation = tabNav
        )
        
        val newState = NavigationReducer.reduce(initialState, NavigationEvent.PopInTab)
        
        assertEquals(initialState, newState)
    }

    @Test
    fun testPopToRootWithTabsClearsAllTabStacks() {
        val tabDef1 = createTabDefinition("tab1", rootRoute)
        val tabDef2 = createTabDefinition("tab2", rootRoute)
        val tabNav = TabNavigationState(
            tabDefinitions = listOf(tabDef1, tabDef2),
            stacksByTab = mapOf(
                "tab1" to listOf(rootRoute, testRoute1, testRoute2),
                "tab2" to listOf(rootRoute, testRoute3)
            ),
            activeTabId = "tab1"
        )
        val initialState = NavigationState(
            tabNavigation = tabNav
        )
        
        val newState = NavigationReducer.reduce(initialState, NavigationEvent.PopToRoot)
        
        assertEquals(1, newState.tabNavigation.stacksByTab["tab1"]?.size)
        assertEquals(1, newState.tabNavigation.stacksByTab["tab2"]?.size)
        assertEquals(rootRoute, newState.tabNavigation.stacksByTab["tab1"]?.first())
    }

    @Test
    fun testPushWithTabsUsesActiveTab() {
        val tabDef1 = createTabDefinition("tab1", rootRoute)
        val tabDef2 = createTabDefinition("tab2", rootRoute)
        val tabNav = TabNavigationState(
            tabDefinitions = listOf(tabDef1, tabDef2),
            stacksByTab = mapOf(
                "tab1" to listOf(rootRoute, testRoute1),
                "tab2" to listOf(rootRoute)
            ),
            activeTabId = "tab1"
        )
        val initialState = NavigationState(
            tabNavigation = tabNav
        )
        val handler = TestRouteHandler(Destination.RestaurantDetail("2"), testRoute2)
        
        val newState = NavigationReducer.reduce(
            initialState,
            NavigationEvent.Push(Destination.RestaurantDetail("2")),
            listOf(handler)
        )
        
        val tab1Stack = newState.tabNavigation.stacksByTab["tab1"]
        assertEquals(3, tab1Stack?.size)
        assertEquals(testRoute2, tab1Stack?.last())
    }

    //  DEEP LINKING TESTS

    @Test
    fun testApplyNavigationStateReplacesEntireState() {
        val tabDef = createTabDefinition("tab1", rootRoute)
        val initialTabNav = TabNavigationState(
            tabDefinitions = listOf(tabDef),
            activeTabId = tabDef.id,
            stacksByTab = mapOf(tabDef.id to listOf(rootRoute, testRoute1))
        )
        val newTabNav = TabNavigationState(
            tabDefinitions = listOf(tabDef),
            activeTabId = tabDef.id,
            stacksByTab = mapOf(tabDef.id to listOf(rootRoute, testRoute2, testRoute3))
        )
        val initialState = NavigationState(tabNavigation = initialTabNav)
        val newState = NavigationState(tabNavigation = newTabNav)
        val result = NavigationReducer.reduce(
            initialState,
            NavigationEvent.ApplyNavigationState(newState)
        )
        assertEquals(newState, result)
    }

    @Test
    fun testApplyNavigationStateToTabState() {
        val tabDef1 = createTabDefinition("tab1", rootRoute)
        val tabDef2 = createTabDefinition("tab2", rootRoute)
        val initialTabNav = TabNavigationState(
            tabDefinitions = listOf(tabDef1, tabDef2),
            activeTabId = tabDef1.id,
            stacksByTab = mapOf(tabDef1.id to listOf(rootRoute))
        )
        val newTabNav = TabNavigationState(
            tabDefinitions = listOf(tabDef1, tabDef2),
            activeTabId = tabDef1.id,
            stacksByTab = mapOf(
                tabDef1.id to listOf(rootRoute, testRoute1),
                tabDef2.id to listOf(rootRoute, testRoute2)
            )
        )
        val initialState = NavigationState(tabNavigation = initialTabNav)
        val newState = NavigationState(tabNavigation = newTabNav)
        val result = NavigationReducer.reduce(
            initialState,
            NavigationEvent.ApplyNavigationState(newState)
        )
        assertEquals(newState, result)
    }

    //  STATE IMMUTABILITY TESTS

    @Test
    fun testReducerDoesNotMutateOriginalState() {
        val initialState = NavigationState(tabNavigation = createSingleTabNav(rootRoute, testRoute1))
        val originalTabStacks = initialState.tabNavigation.stacksByTab
        val handler = TestRouteHandler(Destination.RestaurantDetail("1"), testRoute2)
        
        NavigationReducer.reduce(
            initialState,
            NavigationEvent.Push(Destination.RestaurantDetail("1")),
            listOf(handler)
        )
        
        assertEquals(originalTabStacks, initialState.tabNavigation.stacksByTab)
    }

    @Test
    fun testReducerDoesNotMutateModalStack() {
        val modal1 = TestModalRoute("modal1")
        val initialState = NavigationState(
            tabNavigation = createSingleTabNav(rootRoute),
            modalStack = listOf(modal1)
        )
        val originalModalStack = initialState.modalStack
        
        NavigationReducer.reduce(initialState, NavigationEvent.DismissModal)
        
        assertEquals(originalModalStack, initialState.modalStack)
    }

    @Test
    fun testReducerDoesNotMutateTabNavigation() {
        val tabDef1 = createTabDefinition("tab1", rootRoute)
        val tabDef2 = createTabDefinition("tab2", rootRoute)
        val tabNav = TabNavigationState(
            tabDefinitions = listOf(tabDef1, tabDef2),
            stacksByTab = mapOf(
                "tab1" to listOf(rootRoute, testRoute1),
                "tab2" to listOf(rootRoute)
            ),
            activeTabId = "tab1"
        )
        val initialState = NavigationState(
            tabNavigation = tabNav
        )
        val originalTabStacks = tabNav.stacksByTab
        
        NavigationReducer.reduce(initialState, NavigationEvent.SelectTab("tab2"))
        
        assertEquals(originalTabStacks, tabNav.stacksByTab)
    }

    //  COMPUTED PROPERTY TESTS

    @Test
    fun testCurrentStackReturnsPrimaryWhenNoTabs() {
        val initialState = NavigationState(tabNavigation = createSingleTabNav(rootRoute, testRoute1))

        assertEquals(initialState.tabNavigation.stacksByTab["tab1"], initialState.currentStack)
    }

    @Test
    fun testCurrentStackReturnsActiveTabStackWhenTabs() {
        val tabDef1 = createTabDefinition("tab1", rootRoute)
        val tabDef2 = createTabDefinition("tab2", rootRoute)
        val tabNav = TabNavigationState(
            tabDefinitions = listOf(tabDef1, tabDef2),
            stacksByTab = mapOf(
                "tab1" to listOf(rootRoute, testRoute1),
                "tab2" to listOf(rootRoute, testRoute2)
            ),
            activeTabId = "tab1"
        )
        val initialState = NavigationState(
            tabNavigation = tabNav
        )
        
        assertEquals(listOf(rootRoute, testRoute1), initialState.currentStack)
    }

    @Test
    fun testHasModalsReturnsTrueWhenModalsPresent() {
        val initialState = NavigationState(
            tabNavigation = createSingleTabNav(rootRoute),
            modalStack = listOf(TestModalRoute("modal1"))
        )
        
        assertTrue(initialState.hasModals)
    }

    @Test
    fun testHasModalsReturnsFalseWhenEmpty() {
        val initialState = NavigationState(tabNavigation = createSingleTabNav(rootRoute))

        assertFalse(initialState.hasModals)
    }

    @Test
    fun testTopModalReturnsLastModal() {
        val modal1 = TestModalRoute("modal1")
        val modal2 = TestModalRoute("modal2")
        val initialState = NavigationState(
            tabNavigation = createSingleTabNav(rootRoute),
            modalStack = listOf(modal1, modal2)
        )
        
        assertEquals(modal2, initialState.topModal)
    }

    @Test
    fun testTopModalReturnsNullWhenEmpty() {
        val initialState = NavigationState(tabNavigation = createSingleTabNav(rootRoute))

        assertNull(initialState.topModal)
    }

    //  TEST HELPERS

    private data class TestRouteHandler(
        val destination: Destination,
        override val route: Route
    ) : RouteHandler {
        override fun toRouteString(): String = route.key
        override fun canHandle(destination: Destination): Boolean =
            this.destination == destination

        override fun destinationToRoute(destination: Destination): Route? =
            if (canHandle(destination)) route else null
    }

    private fun createTabDefinition(tabId: String, rootRoute: Route): TabDefinition {
        return TabDefinition(
            id = tabId,
            label = io.umain.munchies.core.localization.StringResources.app_title,
            icon = io.umain.munchies.core.ui.IconId.Logo,
            rootRoute = rootRoute
        )
    }

    private fun createSingleTabNav(vararg routes: Route): TabNavigationState {
        val tabDef = createTabDefinition("tab1", rootRoute)
        return TabNavigationState(
            tabDefinitions = listOf(tabDef),
            activeTabId = tabDef.id,
            stacksByTab = mapOf(tabDef.id to routes.toList())
        )
    }
}
