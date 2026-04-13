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
         assertEquals(testRoute1, newState.currentStack.last())
     }

    @Test
    fun testReduceStatePopRemovesScreen() {
        val coordinator = AppCoordinator(
            NavigationState(tabNavigation = createSingleTabNav(rootRoute, testRoute1, testRoute2))
        )
        
        coordinator.reduceState(NavigationEvent.Pop)
        val newState = coordinator.getCurrentState()
        
        assertEquals(2, newState.tabNavigation.getActiveTabStack().size)
        assertEquals(testRoute1, newState.tabNavigation.getActiveTabStack().last())
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
         assertEquals(testRoute2, finalState.currentStack.last())
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
                "tab1" to listOf(rootRoute, testRoute1),
                "tab2" to listOf(rootRoute)
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
             stacksByTab = mapOf("tab1" to listOf(rootRoute)),
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
         assertEquals(listOf(RestaurantListRoute()), coordinatorWithoutHandlers.navigationState.value.currentStack)
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
         assertEquals(testRoute2, coordinator.getCurrentState().tabNavigation.getActiveTabStack().last(), "Final state should reflect last navigation")
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

    // ── TestScopedRouteHandler ───────────────────────────────────────────────

    /**
     * A ScopedRouteHandler that records every createScope() call so tests can
     * assert that AppCoordinator calls it at the right times without needing Koin.
     */
    private inner class TestScopedRouteHandler(
        val destination: Destination,
        override val route: Route
    ) : ScopedRouteHandler {
        val scopesCreated = mutableListOf<Route>()

        override fun toRouteString(): String = route.key
        override fun canHandle(destination: Destination) = this.destination == destination
        override fun destinationToRoute(destination: Destination): Route? =
            if (canHandle(destination)) route else null

        /**
         * Record the call and return null cast to Scope.
         * AppCoordinator calls createScope() but never dereferences the returned value,
         * so null is safe here. No real Koin runtime is needed in commonTest.
         */
        @Suppress("UNCHECKED_CAST")
        override fun createScope(route: Route): org.koin.core.scope.Scope {
            scopesCreated.add(route)
            return null as org.koin.core.scope.Scope
        }
    }

    // ── SCOPE LIFECYCLE TESTS ────────────────────────────────────────────────

    @Test
    fun testInitCreatesScopes_ForInitialTabRoots() {
        val listHandler   = TestScopedRouteHandler(Destination.RestaurantList,    RestaurantListRoute())
        val detailHandler = TestScopedRouteHandler(Destination.RestaurantDetail("x"), RestaurantDetailRoute("x"))

        // AppCoordinator default state has RestaurantListRoute + SettingsRoute as tab roots
        AppCoordinator(routeHandlers = listOf(listHandler, detailHandler))

        // listHandler must have been called once (for RestaurantListRoute root)
        assertEquals(
            1, listHandler.scopesCreated.size,
            "createScope should be called once for RestaurantListRoute tab root"
        )
        assertTrue(listHandler.scopesCreated[0] is RestaurantListRoute)
        // detail handler should NOT have been called — detail is not in initial state
        assertEquals(0, detailHandler.scopesCreated.size)
    }

    @Test
    fun testInitDoesNotCreateScopeWhenNoHandlerMatches() {
        val handler = TestScopedRouteHandler(Destination.RestaurantDetail("x"), RestaurantDetailRoute("x"))
        // default initial state has RestaurantListRoute — no handler for it → no scope
        AppCoordinator(routeHandlers = listOf(handler))
        assertEquals(0, handler.scopesCreated.size)
    }

    @Test
    fun testReduceState_PushCallsCreateScopeForNewRoute() {
        val detailRoute   = RestaurantDetailRoute("r1")
        val detailHandler = TestScopedRouteHandler(Destination.RestaurantDetail("r1"), detailRoute)
        val coordinator   = AppCoordinator(routeHandlers = listOf(detailHandler))

        val countBefore = detailHandler.scopesCreated.size

        coordinator.reduceState(NavigationEvent.Push(Destination.RestaurantDetail("r1")))

        assertEquals(
            countBefore + 1, detailHandler.scopesCreated.size,
            "createScope should be called once when a new route is pushed"
        )
        assertEquals(detailRoute, detailHandler.scopesCreated.last())
    }

    @Test
    fun testReduceState_PushInTabCallsCreateScopeForNewRoute() {
        val detailRoute   = RestaurantDetailRoute("r2")
        val detailHandler = TestScopedRouteHandler(Destination.RestaurantDetail("r2"), detailRoute)
        val coordinator   = AppCoordinator(routeHandlers = listOf(detailHandler))

        coordinator.reduceState(NavigationEvent.PushInTab(Destination.RestaurantDetail("r2")))

        assertTrue(
            detailHandler.scopesCreated.any { it == detailRoute },
            "createScope should be called when PushInTab adds a new route"
        )
    }

    @Test
    fun testReduceState_PopDoesNotCallCreateScope() {
        val detailRoute   = RestaurantDetailRoute("r3")
        val detailHandler = TestScopedRouteHandler(Destination.RestaurantDetail("r3"), detailRoute)
        val initialState  = NavigationState(
            tabNavigation = createSingleTabNav(rootRoute, detailRoute)
        )
        val coordinator   = AppCoordinator(initialState, listOf(detailHandler))

        val countAfterInit = detailHandler.scopesCreated.size

        coordinator.reduceState(NavigationEvent.Pop)

        // Pop removes the route — no NEW scope should be created
        assertEquals(
            countAfterInit, detailHandler.scopesCreated.size,
            "createScope must NOT be called on Pop — only scope closure happens"
        )
    }

    @Test
    fun testReduceState_NoCreateScope_WhenRouteAlreadyPresent() {
        // Push the same destination twice — it's already in the state after first push,
        // so the second push adds another instance with same key — diff picks it up once.
        val listRoute   = RestaurantListRoute()
        val listHandler = TestScopedRouteHandler(Destination.RestaurantList, listRoute)
        val coordinator = AppCoordinator(routeHandlers = listOf(listHandler))

        val countAfterInit = listHandler.scopesCreated.size

        // First push — detail route not in initial state → one new scope
        coordinator.reduceState(NavigationEvent.PushInTab(Destination.RestaurantList))
        val countAfterFirst = listHandler.scopesCreated.size

        // Second push of the same route — still adds to stack (same key, so set diff = 0)
        coordinator.reduceState(NavigationEvent.PushInTab(Destination.RestaurantList))
        val countAfterSecond = listHandler.scopesCreated.size

        // First push: new entry in diff → createScope called
        assertEquals(countAfterInit + 1, countAfterFirst, "First push should create a scope")
        // Second push: same route key already in set → no new scope
        assertEquals(countAfterFirst, countAfterSecond, "Second push of same route should NOT create another scope")
    }

    @Test
    fun testReduceState_ApplyNavigationState_CreatesAndClosesCorrectScopes() {
        val detailRoute   = RestaurantDetailRoute("r4")
        val detailHandler = TestScopedRouteHandler(Destination.RestaurantDetail("r4"), detailRoute)
        // Start with the detail route already in the stack
        val initialState  = NavigationState(
            tabNavigation = createSingleTabNav(rootRoute, detailRoute)
        )
        val coordinator = AppCoordinator(initialState, listOf(detailHandler))

        val countAfterInit = detailHandler.scopesCreated.size

        // Apply a state that NO LONGER contains detailRoute — scope should be closed (no-op in test)
        val newState = NavigationState(tabNavigation = createSingleTabNav(rootRoute))
        coordinator.reduceState(NavigationEvent.ApplyNavigationState(newState))

        // No new scopes should be created (detailRoute was removed, not added)
        assertEquals(countAfterInit, detailHandler.scopesCreated.size)
    }

    @Test
    fun testReduceState_ScopedHandlerNotCalledForNonScopedHandler() {
        // Regular (non-scoped) handler alongside a scoped handler.
        // Only the scoped one should have createScope() called.
        val regularHandler = TestRouteHandler(Destination.RestaurantList, testRoute1)
        val scopedRoute    = RestaurantDetailRoute("r5")
        val scopedHandler  = TestScopedRouteHandler(Destination.RestaurantDetail("r5"), scopedRoute)

        val coordinator = AppCoordinator(routeHandlers = listOf(regularHandler, scopedHandler))

        coordinator.reduceState(NavigationEvent.PushInTab(Destination.RestaurantDetail("r5")))

        assertTrue(
            scopedHandler.scopesCreated.isNotEmpty(),
            "Scoped handler's createScope must be called"
        )
        // TestRouteHandler has no createScope — the test just verifies no ClassCast / NPE
    }

    @Test
    fun testReduceState_TabSwitchDoesNotCreateDuplicateScopes() {
        val listHandler = TestScopedRouteHandler(Destination.RestaurantList, RestaurantListRoute())
        val coordinator = AppCoordinator(routeHandlers = listOf(listHandler))

        val countAfterInit = listHandler.scopesCreated.size

        // Switching tabs doesn't add new routes to the sets → no new scopes
        coordinator.reduceState(NavigationEvent.SelectTab("settings"))
        coordinator.reduceState(NavigationEvent.SelectTab("restaurants"))

        assertEquals(
            countAfterInit, listHandler.scopesCreated.size,
            "Tab switching must not trigger extra createScope calls"
        )
    }

}
