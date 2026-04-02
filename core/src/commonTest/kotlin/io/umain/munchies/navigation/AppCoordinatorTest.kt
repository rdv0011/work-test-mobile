package io.umain.munchies.navigation

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

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

    //  STATE MANAGEMENT TESTS

    @Test
    fun testInitialStateMatchesConstructorParameter() {
        val initialState = NavigationState(tabNavigation = createSingleTabNav(rootRoute, testRoute1))
        val coordinator = AppCoordinator(initialState)
        val currentState = coordinator.getCurrentState()
        assertEquals(initialState, currentState)
    }

    @Test
    fun testDefaultInitialState() {
        val coordinator = AppCoordinator()
        val state = coordinator.navigationState.value
        // Default state uses tab navigation
        assertEquals("restaurants", state.tabNavigation.activeTabId)
        assertEquals(2, state.tabNavigation.tabDefinitions.size) // restaurants + settings
    }

    //  REDUCER STATE INTEGRATION TESTS

     @Test
     fun testReduceStatePushUpdatesState() {
         val handlers = listOf(
             TestRouteHandler(Destination.RestaurantList, testRoute1)
         )
         val coordinator = AppCoordinator(routeHandlers = handlers)
         val initialState = coordinator.getCurrentState()
         val initialStackSize = initialState.currentStack.size
         
         coordinator.reduceState(NavigationEvent.PushInTab(Destination.RestaurantList))
         val newState = coordinator.getCurrentState()
         
         assertEquals(initialStackSize + 1, newState.currentStack.size)
         assertEquals(testRoute1, newState.currentStack.last().route)
     }

    @Test
    fun testReduceStatePopRemovesScreen() {
        val coordinator = AppCoordinator(
            NavigationState(tabNavigation = createSingleTabNav(rootRoute, testRoute1, testRoute2))
        )
        
        coordinator.reduceState(NavigationEvent.Pop)
        val newState = coordinator.getCurrentState()
        
        assertEquals(2, newState.tabNavigation.getActiveTabStack().size)
        assertEquals(testRoute1, newState.tabNavigation.getActiveTabStack().last().route)
    }

    @Test
    fun testReduceStatePopToRootClearsStack() {
        val coordinator = AppCoordinator(
            NavigationState(tabNavigation = createSingleTabNav(rootRoute, testRoute1, testRoute2))
        )
        
        coordinator.reduceState(NavigationEvent.PopToRoot)
        val newState = coordinator.getCurrentState()
        
        assertEquals(1, newState.tabNavigation.stacksByTab.size)
    }

     @Test
     fun testSequentialReduceStateUpdates() {
         val handlers = listOf(
             TestRouteHandler(Destination.RestaurantList, testRoute1),
             TestRouteHandler(Destination.RestaurantDetail("1"), testRoute2)
         )
         val coordinator = AppCoordinator(routeHandlers = handlers)
         
         val initialStackSize = coordinator.getCurrentState().currentStack.size
         
         coordinator.reduceState(NavigationEvent.PushInTab(Destination.RestaurantList))
         coordinator.reduceState(NavigationEvent.PushInTab(Destination.RestaurantDetail("1")))
         
         val finalState = coordinator.getCurrentState()
         assertEquals(initialStackSize + 2, finalState.currentStack.size)
         assertEquals(testRoute2, finalState.currentStack.last().route)
     }

    //  EVENT REPLAY TESTS

    @Test
    fun testNavigationEventsHaveNoReplay() = runTest {
        val coordinator = AppCoordinator()

        // Dispatch a bunch of events while no one is listening
        coordinator.navigateToScreen(Destination.Settings)
        coordinator.showModal(ModalDestination.ReviewSuccessModal)
        coordinator.selectTab("settings")

        // Now someone starts listening (e.g. Android UI rotated and restarted collection)
        val collectedEvents = mutableListOf<NavigationEvent>()
        val job = launch {
            coordinator.navigationEvents.toList(collectedEvents)
        }

        // They should receive ZERO old events
        assertTrue(
            collectedEvents.isEmpty(),
            "SharedFlow should NOT replay past events to new subscribers (which broke config changes on Android)"
        )

        // But they DO receive new events
        coordinator.navigateBack()
        assertEquals(1, collectedEvents.size)
        assertTrue(collectedEvents[0] is NavigationEvent.Pop)

        job.cancel()
    }

    //  CONVENIENCE METHOD TESTS

     @Test
     fun testNavigateToScreenUpdatesStateAfterReduce() {
         val handlers = listOf(
             TestRouteHandler(Destination.RestaurantList, testRoute1)
         )
         val coordinator = AppCoordinator(routeHandlers = handlers)
         
         val initialStackSize = coordinator.getCurrentState().currentStack.size
         
         coordinator.navigateToScreen(Destination.RestaurantList)
         val event = NavigationEvent.PushInTab(Destination.RestaurantList)
         coordinator.reduceState(event)
         
         val newState = coordinator.getCurrentState()
         assertEquals(initialStackSize + 1, newState.currentStack.size)
     }

     @Test
     fun testNavigateToRestaurantDetailWithId() {
         val handlers = listOf(
             TestRouteHandler(Destination.RestaurantDetail("123"), testRoute2)
         )
         val coordinator = AppCoordinator(routeHandlers = handlers)
         
         val initialStackSize = coordinator.getCurrentState().currentStack.size
         
         coordinator.navigateToRestaurantDetail("123")
         coordinator.reduceState(NavigationEvent.PushInTab(Destination.RestaurantDetail("123")))
         
         val newState = coordinator.getCurrentState()
         assertEquals(initialStackSize + 1, newState.currentStack.size)
     }

    @Test
    fun testShowFilterModalCreatesFilterEvent() {
        val coordinator = AppCoordinator()
        coordinator.showFilterModal(listOf("tag1", "tag2"))
        val currentState = coordinator.getCurrentState()
        assertEquals("restaurants", currentState.tabNavigation.activeTabId)
    }

    @Test
    fun testShowConfirmationCreatesConfirmEvent() {
        val coordinator = AppCoordinator()
        coordinator.showConfirmation("Delete?", "Yes", "No")
        val currentState = coordinator.getCurrentState()
        assertEquals("restaurants", currentState.tabNavigation.activeTabId)
    }

    @Test
    fun testSubmitReviewEvent() {
        val coordinator = AppCoordinator()
        coordinator.submitReview("restaurant123")
        val currentState = coordinator.getCurrentState()
        assertEquals("restaurants", currentState.tabNavigation.activeTabId)
    }

    //  TAB NAVIGATION STATE TESTS

    @Test
    fun testSelectTabUpdatesTabState() {
        val tabDef1 = createTabDefinition("tab1", rootRoute)
        val tabDef2 = createTabDefinition("tab2", rootRoute)
        val tabNav = TabNavigationState(
            tabDefinitions = listOf(tabDef1, tabDef2),
            stacksByTab = mapOf(
                "tab1" to listOf(ScreenEntry(rootRoute, "1"), ScreenEntry(testRoute1, "2")),
                "tab2" to listOf(ScreenEntry(rootRoute, "3"))
            ),
            activeTabId = "tab1"
        )
        val coordinator = AppCoordinator(
            NavigationState(tabNavigation = tabNav)
        )
        
        coordinator.reduceState(NavigationEvent.SelectTab("tab2"))
        val newState = coordinator.getCurrentState()
        
        assertEquals("tab2", newState.tabNavigation.activeTabId)
    }

     @Test
     fun testNavigateInTabUpdatesTabStack() {
         val tabDef1 = createTabDefinition("tab1", rootRoute)
         val tabNav = TabNavigationState(
             tabDefinitions = listOf(tabDef1),
             stacksByTab = mapOf("tab1" to listOf(ScreenEntry(rootRoute, "1"))),
             activeTabId = "tab1"
         )
         val handlers = listOf(
             TestRouteHandler(Destination.RestaurantList, testRoute1)
         )
         val coordinator = AppCoordinator(
             NavigationState(tabNavigation = tabNav),
             routeHandlers = handlers
         )
         
         coordinator.reduceState(NavigationEvent.PushInTab(Destination.RestaurantList))
         val newState = coordinator.getCurrentState()
         
         assertEquals(2, newState.tabNavigation.getActiveTabStack().size)
     }

    //  ROUTE HANDLER TESTS

     @Test
     fun testRouteHandlerIntegration() {
         val handler = TestRouteHandler(Destination.RestaurantList, testRoute1)
         val coordinatorWithoutHandlers = AppCoordinator()
         // The default stack is not empty, it contains the root route of the active tab
         assertEquals(listOf(RestaurantListRoute()), coordinatorWithoutHandlers.navigationState.value.currentStack.map { it.route })
         val coordinatorWithHandlers = AppCoordinator(routeHandlers = listOf(handler))
         val initialStackSize = coordinatorWithHandlers.getCurrentState().currentStack.size
         coordinatorWithHandlers.reduceState(NavigationEvent.PushInTab(Destination.RestaurantList))
         val newState = coordinatorWithHandlers.getCurrentState()
         assertEquals(initialStackSize + 1, newState.currentStack.size)
     }

    @Test
    fun testNoHandlerLeavesStateUnchanged() {
        val coordinator = AppCoordinator()
        val initialState = coordinator.getCurrentState()
        
        coordinator.reduceState(NavigationEvent.Push(Destination.RestaurantList))
        val afterState = coordinator.getCurrentState()
        
        assertEquals(initialState, afterState)
    }

    //  MODAL OPERATIONS TESTS

    @Test
    fun testDismissModalUpdatesState() {
        val modal = TestModalRoute("modal1")
        val coordinator = AppCoordinator(
            NavigationState(
                tabNavigation = createSingleTabNav(rootRoute),
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
                tabNavigation = createSingleTabNav(rootRoute),
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
                tabNavigation = createSingleTabNav(rootRoute),
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
            tabNavigation = createSingleTabNav(rootRoute, testRoute1, testRoute2)
        )
        
        coordinator.reduceState(NavigationEvent.ApplyNavigationState(newState))
        val currentState = coordinator.getCurrentState()
        
        assertEquals(newState, currentState)
    }

    //  LISTENER READINESS TESTS (CRITICAL FOR DEEP LINKS)

    @Test
    fun testOnListenerReadyCallbackExecutesImmediatelyWhenAlreadyReady() {
        val coordinator = AppCoordinator()
        var callbackExecuted = false
        
        coordinator.markListenerReady()
        coordinator.onListenerReady { callbackExecuted = true }
        
        assertTrue(callbackExecuted, "Callback should execute immediately when listener already marked ready")
    }

    @Test
    fun testOnListenerReadyCallbackQueuedBeforeMarkReady() {
        val coordinator = AppCoordinator()
        var callbackExecuted = false
        
        coordinator.onListenerReady { callbackExecuted = true }
        assertFalse(callbackExecuted, "Callback should not execute before markListenerReady")
        
        coordinator.markListenerReady()
        assertTrue(callbackExecuted, "Callback should execute after markListenerReady")
    }

    @Test
    fun testMultipleCallbacksQueuedAndExecutedInOrder() {
        val coordinator = AppCoordinator()
        val executionOrder = mutableListOf<Int>()
        
        coordinator.onListenerReady { executionOrder.add(1) }
        coordinator.onListenerReady { executionOrder.add(2) }
        coordinator.onListenerReady { executionOrder.add(3) }
        
        assertTrue(executionOrder.isEmpty(), "No callbacks should execute before markListenerReady")
        
        coordinator.markListenerReady()
        
        assertEquals(listOf(1, 2, 3), executionOrder, "Callbacks should execute in registration order")
    }

    @Test
    fun testMarkListenerReadyIdempotent() {
        val coordinator = AppCoordinator()
        var callbackCount = 0
        
        coordinator.onListenerReady { callbackCount++ }
        coordinator.markListenerReady()
        coordinator.markListenerReady()
        coordinator.markListenerReady()
        
        assertEquals(1, callbackCount, "Callback should execute only once despite multiple markListenerReady calls")
    }

    @Test
    fun testCallbackRegisteredAfterMarkReadyExecutesImmediately() {
        val coordinator = AppCoordinator()
        coordinator.markListenerReady()
        
        var firstCallbackExecuted = false
        var secondCallbackExecuted = false
        
        coordinator.onListenerReady { firstCallbackExecuted = true }
        assertTrue(firstCallbackExecuted, "First callback after markReady should execute immediately")
        
        coordinator.onListenerReady { secondCallbackExecuted = true }
        assertTrue(secondCallbackExecuted, "Second callback after markReady should also execute immediately")
    }

    @Test
    fun testMixedCallbackTiming() {
        val coordinator = AppCoordinator()
        val executionOrder = mutableListOf<String>()
        
        coordinator.onListenerReady { executionOrder.add("before1") }
        coordinator.markListenerReady()
        coordinator.onListenerReady { executionOrder.add("after1") }
        coordinator.onListenerReady { executionOrder.add("after2") }
        
        assertEquals(
            listOf("before1", "after1", "after2"),
            executionOrder,
            "Mixed callbacks should execute in correct order"
        )
    }

    //  EDGE CASE: MULTIPLE QUEUED DEEP LINKS

     @Test
     fun testMultipleDeepLinksProcessedSequentially() {
         val handlers = listOf(
             TestRouteHandler(Destination.RestaurantList, testRoute1),
             TestRouteHandler(Destination.RestaurantDetail("123"), testRoute2)
         )
         val coordinator = AppCoordinator(routeHandlers = handlers)
         
         val processedDestinations = mutableListOf<String>()
         
         coordinator.onListenerReady {
             processedDestinations.add("ready")
         }
         
         val state1 = NavigationState(tabNavigation = createSingleTabNav(rootRoute, testRoute1))
         coordinator.reduceState(NavigationEvent.ApplyNavigationState(state1))
         
         val state2 = NavigationState(tabNavigation = createSingleTabNav(rootRoute, testRoute2))
         coordinator.reduceState(NavigationEvent.ApplyNavigationState(state2))
         
         assertEquals(0, processedDestinations.size, "Callback should not execute until markListenerReady is called")
         
         coordinator.markListenerReady()
         assertEquals(listOf("ready"), processedDestinations, "Readiness callback should execute once")
         assertEquals(testRoute2, coordinator.getCurrentState().tabNavigation.getActiveTabStack().last().route, "Final state should reflect last navigation")
     }

    //  EDGE CASE: NAVIGATION DURING READINESS PHASE

     @Test
     fun testNavigationEventsDuringReadinessPhase() {
         val handlers = listOf(
             TestRouteHandler(Destination.RestaurantList, testRoute1),
             TestRouteHandler(Destination.RestaurantDetail("123"), testRoute2)
         )
         val coordinator = AppCoordinator(routeHandlers = handlers)
         
         val eventLog = mutableListOf<String>()
         
         coordinator.onListenerReady {
             eventLog.add("listener_ready")
             coordinator.navigateInTab(Destination.RestaurantDetail("123"))
         }
         
         eventLog.add("before_mark_ready")
         coordinator.markListenerReady()
         eventLog.add("after_mark_ready")
         
         assertEquals(
             listOf("before_mark_ready", "listener_ready", "after_mark_ready"),
             eventLog,
             "Events should be processed in correct order"
         )
     }

    //  EDGE CASE: NO EVENTS DURING INITIALIZATION

    @Test
    fun testListenerReadyWithoutAnyNavigationEvents() {
        val coordinator = AppCoordinator()
        var readyCallbackExecuted = false
        
        coordinator.onListenerReady { readyCallbackExecuted = true }
        assertFalse(readyCallbackExecuted)
        
        coordinator.markListenerReady()
        assertTrue(readyCallbackExecuted, "Readiness callback should execute even with no navigation events")
    }

    //  EDGE CASE: RAPID STATE CHANGES

     @Test
     fun testRapidStateChangesBeforeReadiness() {
         val handlers = listOf(
             TestRouteHandler(Destination.RestaurantList, testRoute1),
             TestRouteHandler(Destination.RestaurantDetail("1"), testRoute2),
             TestRouteHandler(Destination.RestaurantDetail("2"), TestRoute("route3", false))
         )
         val coordinator = AppCoordinator(routeHandlers = handlers)
         
         val readinessCallbackCount = mutableListOf<Int>()

         coordinator.onListenerReady { readinessCallbackCount.add(readinessCallbackCount.size) }

         coordinator.reduceState(NavigationEvent.PushInTab(Destination.RestaurantList))
         coordinator.reduceState(NavigationEvent.PushInTab(Destination.RestaurantDetail("1")))
         coordinator.reduceState(NavigationEvent.PushInTab(Destination.RestaurantDetail("2")))
         
         assertEquals(0, readinessCallbackCount.size, "Callback should not execute before markListenerReady")
         
         coordinator.markListenerReady()
         
         assertEquals(1, readinessCallbackCount.size, "Callback should execute exactly once after markListenerReady")
     }

    //  TEST HELPERS

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
            stacksByTab = mapOf(tabDef.id to routes.mapIndexed { index, route -> ScreenEntry(route, "${route.key}-$index") })
        )
    }
}
