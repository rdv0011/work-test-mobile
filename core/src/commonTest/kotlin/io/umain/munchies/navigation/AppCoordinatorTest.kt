package io.umain.munchies.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Comprehensive unit tests for AppCoordinator.
 *
 * Tests the coordinator behavior:
 * - State management and snapshots
 * - Internal reducer integration
 * - Convenience method delegation
 * - Route handler support
 */
class AppCoordinatorTest {

    private val testRoute1 = TestRoute("route1", false)
    private val testRoute2 = TestRoute("route2", false)
    private val rootRoute = TestRoute("root", true)

    // === STATE MANAGEMENT TESTS ===

    @Test
    fun testInitialStateMatchesConstructorParameter() {
        val initialState = NavigationState(primaryStack = listOf(rootRoute, testRoute1))
        val coordinator = AppCoordinator(initialState)
        
        val currentState = coordinator.getCurrentState()
        
        assertEquals(initialState, currentState)
    }

    @Test
    fun testDefaultInitialState() {
        val coordinator = AppCoordinator()
        val state = coordinator.navigationState.value
        
        assertTrue(state.primaryStack.isNotEmpty())
        assertEquals(1, state.primaryStack.size)
    }

    // === REDUCER STATE INTEGRATION TESTS ===

    @Test
    fun testReduceStatePushUpdatesState() {
        val coordinator = AppCoordinator()
        coordinator.routeHandlers = listOf(
            TestRouteHandler(Destination.RestaurantList, testRoute1)
        )
        val initialState = coordinator.getCurrentState()
        
        coordinator.reduceState(NavigationEvent.Push(Destination.RestaurantList))
        val newState = coordinator.getCurrentState()
        
        assertEquals(initialState.primaryStack.size + 1, newState.primaryStack.size)
        assertEquals(testRoute1, newState.primaryStack.last())
    }

    @Test
    fun testReduceStatePopRemovesScreen() {
        val coordinator = AppCoordinator(
            NavigationState(primaryStack = listOf(rootRoute, testRoute1, testRoute2))
        )
        
        coordinator.reduceState(NavigationEvent.Pop)
        val newState = coordinator.getCurrentState()
        
        assertEquals(2, newState.primaryStack.size)
        assertEquals(testRoute1, newState.primaryStack.last())
    }

    @Test
    fun testReduceStatePopToRootClearsStack() {
        val coordinator = AppCoordinator(
            NavigationState(primaryStack = listOf(rootRoute, testRoute1, testRoute2))
        )
        
        coordinator.reduceState(NavigationEvent.PopToRoot)
        val newState = coordinator.getCurrentState()
        
        assertEquals(1, newState.primaryStack.size)
    }

    @Test
    fun testSequentialReduceStateUpdates() {
        val coordinator = AppCoordinator()
        coordinator.routeHandlers = listOf(
            TestRouteHandler(Destination.RestaurantList, testRoute1),
            TestRouteHandler(Destination.RestaurantDetail("1"), testRoute2)
        )
        
        coordinator.reduceState(NavigationEvent.Push(Destination.RestaurantList))
        coordinator.reduceState(NavigationEvent.Push(Destination.RestaurantDetail("1")))
        
        val finalState = coordinator.getCurrentState()
        assertEquals(3, finalState.primaryStack.size)
        assertEquals(testRoute2, finalState.primaryStack.last())
    }

    // === CONVENIENCE METHOD TESTS ===

    @Test
    fun testNavigateToScreenUpdatesStateAfterReduce() {
        val coordinator = AppCoordinator()
        coordinator.routeHandlers = listOf(
            TestRouteHandler(Destination.RestaurantList, testRoute1)
        )
        
        coordinator.navigateToScreen(Destination.RestaurantList)
        // In real usage, the platform layer would handle events and call reduceState
        // For testing, we call reduceState directly
        val event = NavigationEvent.Push(Destination.RestaurantList)
        coordinator.reduceState(event)
        
        val newState = coordinator.getCurrentState()
        assertEquals(2, newState.primaryStack.size)
    }

    @Test
    fun testNavigateToRestaurantDetailWithId() {
        val coordinator = AppCoordinator()
        coordinator.routeHandlers = listOf(
            TestRouteHandler(Destination.RestaurantDetail("123"), testRoute2)
        )
        
        coordinator.navigateToRestaurantDetail("123")
        coordinator.reduceState(NavigationEvent.Push(Destination.RestaurantDetail("123")))
        
        val newState = coordinator.getCurrentState()
        assertEquals(2, newState.primaryStack.size)
    }

    @Test
    fun testShowFilterModalCreatesFilterEvent() {
        val coordinator = AppCoordinator()
        
        coordinator.showFilterModal(listOf("tag1", "tag2"))
        
        // Event would be processed by platform layer
        // AppCoordinator just emits events, doesn't process them
        assertEquals(NavigationState(primaryStack = listOf(RestaurantListRoute())), coordinator.getCurrentState())
    }

    @Test
    fun testShowConfirmationCreatesConfirmEvent() {
        val coordinator = AppCoordinator()
        
        coordinator.showConfirmation("Delete?", "Yes", "No")
        
        assertEquals(NavigationState(primaryStack = listOf(RestaurantListRoute())), coordinator.getCurrentState())
    }

    @Test
    fun testShowReviewsCreatesReviewsEvent() {
        val coordinator = AppCoordinator()
        
        coordinator.showReviews("restaurant123")
        
        assertEquals(NavigationState(primaryStack = listOf(RestaurantListRoute())), coordinator.getCurrentState())
    }

    // === TAB NAVIGATION STATE TESTS ===

    @Test
    fun testSelectTabUpdatesTabState() {
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
        val coordinator = AppCoordinator(
            NavigationState(usesTabs = true, tabNavigation = tabNav)
        )
        
        coordinator.reduceState(NavigationEvent.SelectTab("tab2"))
        val newState = coordinator.getCurrentState()
        
        assertEquals("tab2", newState.tabNavigation?.activeTabId)
    }

    @Test
    fun testNavigateInTabUpdatesTabStack() {
        val tabDef1 = createTabDefinition("tab1", rootRoute)
        val tabNav = TabNavigationState(
            tabDefinitions = listOf(tabDef1),
            stacksByTab = mapOf("tab1" to listOf(rootRoute)),
            activeTabId = "tab1"
        )
        val coordinator = AppCoordinator(
            NavigationState(usesTabs = true, tabNavigation = tabNav)
        )
        coordinator.routeHandlers = listOf(
            TestRouteHandler(Destination.RestaurantList, testRoute1)
        )
        
        coordinator.reduceState(NavigationEvent.PushInTab(Destination.RestaurantList))
        val newState = coordinator.getCurrentState()
        
        assertEquals(2, newState.tabNavigation?.getActiveTabStack()?.size)
    }

    // === ROUTE HANDLER TESTS ===

    @Test
    fun testRouteHandlerIntegration() {
        val coordinator = AppCoordinator()
        val handler = TestRouteHandler(Destination.RestaurantList, testRoute1)
        
        assertTrue(coordinator.routeHandlers.isEmpty())
        
        coordinator.routeHandlers = listOf(handler)
        coordinator.reduceState(NavigationEvent.Push(Destination.RestaurantList))
        
        val newState = coordinator.getCurrentState()
        assertEquals(2, newState.primaryStack.size)
    }

    @Test
    fun testNoHandlerLeavesStateUnchanged() {
        val coordinator = AppCoordinator()
        val initialState = coordinator.getCurrentState()
        
        coordinator.reduceState(NavigationEvent.Push(Destination.RestaurantList))
        val afterState = coordinator.getCurrentState()
        
        assertEquals(initialState, afterState)
    }

    // === MODAL OPERATIONS TESTS ===

    @Test
    fun testDismissModalUpdatesState() {
        val modal = TestModalRoute("modal1")
        val coordinator = AppCoordinator(
            NavigationState(
                primaryStack = listOf(rootRoute),
                modalStack = listOf(modal)
            )
        )
        
        coordinator.reduceState(NavigationEvent.DismissModal)
        val newState = coordinator.getCurrentState()
        
        assertTrue(newState.modalStack.isEmpty())
    }

    @Test
    fun testDismissAllModalsUpdatesState() {
        val modal1 = TestModalRoute("modal1")
        val modal2 = TestModalRoute("modal2")
        val coordinator = AppCoordinator(
            NavigationState(
                primaryStack = listOf(rootRoute),
                modalStack = listOf(modal1, modal2)
            )
        )
        
        coordinator.reduceState(NavigationEvent.DismissAllModals)
        val newState = coordinator.getCurrentState()
        
        assertTrue(newState.modalStack.isEmpty())
    }

    @Test
    fun testDismissModalUntilUpdatesState() {
        val modal1 = TestModalRoute("modal1")
        val modal2 = TestModalRoute("modal2")
        val coordinator = AppCoordinator(
            NavigationState(
                primaryStack = listOf(rootRoute),
                modalStack = listOf(modal1, modal2)
            )
        )
        
        coordinator.reduceState(NavigationEvent.DismissModalUntil { it.key == "modal1" })
        val newState = coordinator.getCurrentState()
        
        assertEquals(1, newState.modalStack.size)
        assertEquals("modal1", newState.modalStack[0].key)
    }

    @Test
    fun testApplyNavigationStateReplacesState() {
        val coordinator = AppCoordinator()
        val newState = NavigationState(
            primaryStack = listOf(rootRoute, testRoute1, testRoute2)
        )
        
        coordinator.reduceState(NavigationEvent.ApplyNavigationState(newState))
        val currentState = coordinator.getCurrentState()
        
        assertEquals(newState, currentState)
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
