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

    // === SCREEN NAVIGATION TESTS ===

    @Test
    fun testPushWithoutHandlersDoesNothing() {
        val initialState = NavigationState(primaryStack = listOf(rootRoute))
        val event = NavigationEvent.Push(Destination.RestaurantList)
        
        val newState = NavigationReducer.reduce(initialState, event, emptyList())
        
        assertEquals(initialState, newState)
    }

    @Test
    fun testPushWithValidHandlerAddsToStack() {
        val initialState = NavigationState(primaryStack = listOf(rootRoute))
        val handler = TestRouteHandler(Destination.RestaurantList, testRoute1)
        val event = NavigationEvent.Push(Destination.RestaurantList)
        
        val newState = NavigationReducer.reduce(initialState, event, listOf(handler))
        
        assertEquals(2, newState.primaryStack.size)
        assertEquals(testRoute1, newState.primaryStack.last())
    }

    @Test
    fun testPushMultipleScreensBuildsStack() {
        val initialState = NavigationState(primaryStack = listOf(rootRoute))
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
        
        assertEquals(3, state.primaryStack.size)
        assertEquals(testRoute2, state.primaryStack.last())
    }

    @Test
    fun testPopRemovesTopScreen() {
        val initialState = NavigationState(primaryStack = listOf(rootRoute, testRoute1, testRoute2))
        
        val newState = NavigationReducer.reduce(initialState, NavigationEvent.Pop)
        
        assertEquals(2, newState.primaryStack.size)
        assertEquals(testRoute1, newState.primaryStack.last())
    }

    @Test
    fun testPopAtRootDoesNothing() {
        val initialState = NavigationState(primaryStack = listOf(rootRoute))
        
        val newState = NavigationReducer.reduce(initialState, NavigationEvent.Pop)
        
        assertEquals(initialState, newState)
    }

    @Test
    fun testPopToRootClearsStack() {
        val initialState = NavigationState(primaryStack = listOf(rootRoute, testRoute1, testRoute2, testRoute3))
        
        val newState = NavigationReducer.reduce(initialState, NavigationEvent.PopToRoot)
        
        assertEquals(1, newState.primaryStack.size)
        assertEquals(rootRoute, newState.primaryStack.first())
    }

    @Test
    fun testPopToRootWithSingleScreenDoesNothing() {
        val initialState = NavigationState(primaryStack = listOf(rootRoute))
        
        val newState = NavigationReducer.reduce(initialState, NavigationEvent.PopToRoot)
        
        assertEquals(initialState, newState)
    }

    // === MODAL NAVIGATION TESTS ===

    @Test
    fun testDismissModalRemovesTopModal() {
        val modal1 = TestModalRoute("modal1")
        val modal2 = TestModalRoute("modal2")
        val initialState = NavigationState(
            primaryStack = listOf(rootRoute, testRoute1),
            modalStack = listOf(modal1, modal2)
        )
        
        val newState = NavigationReducer.reduce(initialState, NavigationEvent.DismissModal)
        
        assertEquals(1, newState.modalStack.size)
        assertEquals(modal1, newState.modalStack.first())
    }

    @Test
    fun testDismissModalWithEmptyStackDoesNothing() {
        val initialState = NavigationState(primaryStack = listOf(rootRoute))
        
        val newState = NavigationReducer.reduce(initialState, NavigationEvent.DismissModal)
        
        assertEquals(initialState, newState)
    }

    @Test
    fun testDismissAllModalsClearsStack() {
        val modal1 = TestModalRoute("modal1")
        val modal2 = TestModalRoute("modal2")
        val modal3 = TestModalRoute("modal3")
        val initialState = NavigationState(
            primaryStack = listOf(rootRoute, testRoute1),
            modalStack = listOf(modal1, modal2, modal3)
        )
        
        val newState = NavigationReducer.reduce(initialState, NavigationEvent.DismissAllModals)
        
        assertTrue(newState.modalStack.isEmpty())
        assertEquals(initialState.primaryStack, newState.primaryStack)
    }

    @Test
    fun testDismissAllModalsWithEmptyStackDoesNothing() {
        val initialState = NavigationState(primaryStack = listOf(rootRoute))
        
        val newState = NavigationReducer.reduce(initialState, NavigationEvent.DismissAllModals)
        
        assertEquals(initialState, newState)
    }

    @Test
    fun testDismissModalUntilMatchingPredicate() {
        val modal1 = TestModalRoute("modal1")
        val modal2 = TestModalRoute("modal2")
        val modal3 = TestModalRoute("modal3")
        val initialState = NavigationState(
            primaryStack = listOf(rootRoute),
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
            primaryStack = listOf(rootRoute),
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
            primaryStack = listOf(rootRoute, testRoute1),
            modalStack = listOf(modal)
        )
        
        val newState = NavigationReducer.reduce(initialState, NavigationEvent.Pop)
        
        assertTrue(newState.modalStack.isEmpty())
        assertEquals(initialState.primaryStack, newState.primaryStack)
    }

    // === TAB NAVIGATION TESTS ===

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
            usesTabs = true,
            tabNavigation = tabNav
        )
        
        val newState = NavigationReducer.reduce(
            initialState,
            NavigationEvent.SelectTab("tab2")
        )
        
        assertEquals("tab2", newState.tabNavigation?.activeTabId)
        // Primary stack should remain unchanged
        assertEquals(initialState.tabNavigation?.stacksByTab, newState.tabNavigation?.stacksByTab)
    }

    @Test
    fun testSelectTabWithoutTabNavigationDoesNothing() {
        val initialState = NavigationState(primaryStack = listOf(rootRoute))
        
        val newState = NavigationReducer.reduce(
            initialState,
            NavigationEvent.SelectTab("tab1")
        )
        
        assertEquals(initialState, newState)
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
            usesTabs = true,
            tabNavigation = tabNav
        )
        val handler = TestRouteHandler(Destination.RestaurantList, testRoute2)
        
        val newState = NavigationReducer.reduce(
            initialState,
            NavigationEvent.PushInTab(Destination.RestaurantList),
            listOf(handler)
        )
        
        val tab1Stack = newState.tabNavigation?.stacksByTab?.get("tab1")
        assertEquals(3, tab1Stack?.size)
        assertEquals(testRoute2, tab1Stack?.last())
        // Other tab unaffected
        assertEquals(1, newState.tabNavigation?.stacksByTab?.get("tab2")?.size)
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
            usesTabs = true,
            tabNavigation = tabNav
        )
        
        val newState = NavigationReducer.reduce(initialState, NavigationEvent.PopInTab)
        
        val tab1Stack = newState.tabNavigation?.stacksByTab?.get("tab1")
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
            usesTabs = true,
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
            usesTabs = true,
            tabNavigation = tabNav
        )
        
        val newState = NavigationReducer.reduce(initialState, NavigationEvent.PopToRoot)
        
        assertEquals(1, newState.tabNavigation?.stacksByTab?.get("tab1")?.size)
        assertEquals(1, newState.tabNavigation?.stacksByTab?.get("tab2")?.size)
        assertEquals(rootRoute, newState.tabNavigation?.stacksByTab?.get("tab1")?.first())
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
            usesTabs = true,
            tabNavigation = tabNav
        )
        val handler = TestRouteHandler(Destination.RestaurantDetail("2"), testRoute2)
        
        val newState = NavigationReducer.reduce(
            initialState,
            NavigationEvent.Push(Destination.RestaurantDetail("2")),
            listOf(handler)
        )
        
        val tab1Stack = newState.tabNavigation?.stacksByTab?.get("tab1")
        assertEquals(3, tab1Stack?.size)
        assertEquals(testRoute2, tab1Stack?.last())
    }

    // === DEEP LINKING TESTS ===

    @Test
    fun testApplyNavigationStateReplacesEntireState() {
        val initialState = NavigationState(
            primaryStack = listOf(rootRoute, testRoute1),
            modalStack = listOf(TestModalRoute("modal1")),
            usesTabs = false
        )
        val newState = NavigationState(
            primaryStack = listOf(rootRoute, testRoute2, testRoute3),
            usesTabs = false
        )
        
        val result = NavigationReducer.reduce(
            initialState,
            NavigationEvent.ApplyNavigationState(newState)
        )
        
        assertEquals(newState, result)
    }

    @Test
    fun testApplyNavigationStateToTabState() {
        val initialState = NavigationState(
            primaryStack = listOf(rootRoute),
            usesTabs = false
        )
        val tabDef1 = createTabDefinition("tab1", rootRoute)
        val tabDef2 = createTabDefinition("tab2", rootRoute)
        val newTabNav = TabNavigationState(
            tabDefinitions = listOf(tabDef1, tabDef2),
            stacksByTab = mapOf(
                "tab1" to listOf(rootRoute, testRoute1),
                "tab2" to listOf(rootRoute, testRoute2)
            ),
            activeTabId = "tab1"
        )
        val newState = NavigationState(
            usesTabs = true,
            tabNavigation = newTabNav
        )
        
        val result = NavigationReducer.reduce(
            initialState,
            NavigationEvent.ApplyNavigationState(newState)
        )
        
        assertEquals(newState, result)
    }

    // === STATE IMMUTABILITY TESTS ===

    @Test
    fun testReducerDoesNotMutateOriginalState() {
        val initialState = NavigationState(primaryStack = listOf(rootRoute, testRoute1))
        val originalPrimaryStack = initialState.primaryStack
        val handler = TestRouteHandler(Destination.RestaurantDetail("1"), testRoute2)
        
        NavigationReducer.reduce(
            initialState,
            NavigationEvent.Push(Destination.RestaurantDetail("1")),
            listOf(handler)
        )
        
        assertEquals(originalPrimaryStack, initialState.primaryStack)
    }

    @Test
    fun testReducerDoesNotMutateModalStack() {
        val modal1 = TestModalRoute("modal1")
        val initialState = NavigationState(
            primaryStack = listOf(rootRoute),
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
            usesTabs = true,
            tabNavigation = tabNav
        )
        val originalTabStacks = tabNav.stacksByTab
        
        NavigationReducer.reduce(initialState, NavigationEvent.SelectTab("tab2"))
        
        assertEquals(originalTabStacks, tabNav.stacksByTab)
    }

    // === COMPUTED PROPERTY TESTS ===

    @Test
    fun testCurrentStackReturnsPrimaryWhenNoTabs() {
        val initialState = NavigationState(primaryStack = listOf(rootRoute, testRoute1))
        
        assertEquals(initialState.primaryStack, initialState.currentStack)
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
            usesTabs = true,
            tabNavigation = tabNav
        )
        
        assertEquals(listOf(rootRoute, testRoute1), initialState.currentStack)
    }

    @Test
    fun testHasModalsReturnsTrueWhenModalsPresent() {
        val initialState = NavigationState(
            primaryStack = listOf(rootRoute),
            modalStack = listOf(TestModalRoute("modal1"))
        )
        
        assertTrue(initialState.hasModals)
    }

    @Test
    fun testHasModalsReturnsFalseWhenEmpty() {
        val initialState = NavigationState(primaryStack = listOf(rootRoute))
        
        assertFalse(initialState.hasModals)
    }

    @Test
    fun testTopModalReturnsLastModal() {
        val modal1 = TestModalRoute("modal1")
        val modal2 = TestModalRoute("modal2")
        val initialState = NavigationState(
            primaryStack = listOf(rootRoute),
            modalStack = listOf(modal1, modal2)
        )
        
        assertEquals(modal2, initialState.topModal)
    }

    @Test
    fun testTopModalReturnsNullWhenEmpty() {
        val initialState = NavigationState(primaryStack = listOf(rootRoute))
        
        assertNull(initialState.topModal)
    }

    // === TEST HELPERS ===

    private data class TestRoute(
        override val key: String,
        override val isRootRoute: Boolean
    ) : StackRoute

    private data class TestModalRoute(
        override val key: String
    ) : ModalRoute {
        override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.SHEET
    }

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
            label = io.umain.munchies.core.ui.TextId.RestaurantListTitle,
            icon = io.umain.munchies.core.ui.IconId.Logo,
            rootRoute = rootRoute
        )
    }
}
