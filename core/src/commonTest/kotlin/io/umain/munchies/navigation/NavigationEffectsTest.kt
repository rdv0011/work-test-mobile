package io.umain.munchies.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for NavigationEffects and the getAllRoutes() extension.
 *
 * getAllRoutes() is the helper AppCoordinator uses to diff old vs new state
 * and decide which Koin scopes to open/close.
 *
 * handleNavigationSideEffects() itself delegates to the expect fun
 * getKoinScopeOrNull(), which has no-op platform actuals in commonTest —
 * so these tests focus on the diffing logic by verifying which routes
 * appear / disappear between states.
 */
class NavigationEffectsTest {

    private val root    = TestRoute("root",    isRootRoute = true)
    private val route1  = TestRoute("route1",  isRootRoute = false)
    private val route2  = TestRoute("route2",  isRootRoute = false)
    private val modal1  = TestModalRoute("modal1")
    private val modal2  = TestModalRoute("modal2")

    // ── getAllRoutes() — tab stacks ──────────────────────────────────────────

    @Test
    fun testGetAllRoutesReturnsTabStackRoutes() {
        val state = stateWith(tabStack = listOf(root, route1))
        val routes = state.getAllRoutes()
        assertTrue(routes.contains(root))
        assertTrue(routes.contains(route1))
    }

    @Test
    fun testGetAllRoutesIncludesAllTabs() {
        val tab1 = makeTabNav(
            mapOf("t1" to listOf(root, route1), "t2" to listOf(root, route2)),
            active = "t1"
        )
        val state = NavigationState(tabNavigation = tab1)
        val routes = state.getAllRoutes()
        assertTrue(routes.contains(route1))
        assertTrue(routes.contains(route2))
    }

    @Test
    fun testGetAllRoutesIncludesModals() {
        val state = stateWith(tabStack = listOf(root), modals = listOf(modal1, modal2))
        val routes = state.getAllRoutes()
        assertTrue(routes.contains(modal1))
        assertTrue(routes.contains(modal2))
    }

    @Test
    fun testGetAllRoutesEmptyStateReturnsEmptySet() {
        val state = stateWith(tabStack = emptyList())
        assertTrue(state.getAllRoutes().isEmpty())
    }

    @Test
    fun testGetAllRoutesReturnsSet_NoDuplicates() {
        // Same route instance in two tabs — set should deduplicate
        val tab = makeTabNav(
            mapOf("t1" to listOf(root), "t2" to listOf(root)),
            active = "t1"
        )
        val state = NavigationState(tabNavigation = tab)
        val routes = state.getAllRoutes()
        // root appears in both tabs but the Set should contain it only once
        assertEquals(1, routes.count { it.key == "root" })
    }

    // ── Route diff logic (added / removed sets) ──────────────────────────────

    @Test
    fun testAddedRoutesDiffCorrectlyOnPush() {
        val before = stateWith(tabStack = listOf(root))
        val after  = stateWith(tabStack = listOf(root, route1))
        val added   = after.getAllRoutes() - before.getAllRoutes()
        val removed = before.getAllRoutes() - after.getAllRoutes()
        assertEquals(setOf(route1), added)
        assertTrue(removed.isEmpty())
    }

    @Test
    fun testRemovedRoutesDiffCorrectlyOnPop() {
        val before = stateWith(tabStack = listOf(root, route1))
        val after  = stateWith(tabStack = listOf(root))
        val added   = after.getAllRoutes() - before.getAllRoutes()
        val removed = before.getAllRoutes() - after.getAllRoutes()
        assertTrue(added.isEmpty())
        assertEquals(setOf(route1), removed)
    }

    @Test
    fun testNoDiffWhenStateIsUnchanged() {
        val state = stateWith(tabStack = listOf(root, route1))
        val added   = state.getAllRoutes() - state.getAllRoutes()
        val removed = state.getAllRoutes() - state.getAllRoutes()
        assertTrue(added.isEmpty())
        assertTrue(removed.isEmpty())
    }

    @Test
    fun testModalAddedIsDetectedInDiff() {
        val before = stateWith(tabStack = listOf(root))
        val after  = stateWith(tabStack = listOf(root), modals = listOf(modal1))
        val added = after.getAllRoutes() - before.getAllRoutes()
        assertTrue(added.contains(modal1))
    }

    @Test
    fun testModalRemovedIsDetectedInDiff() {
        val before = stateWith(tabStack = listOf(root), modals = listOf(modal1))
        val after  = stateWith(tabStack = listOf(root))
        val removed = before.getAllRoutes() - after.getAllRoutes()
        assertTrue(removed.contains(modal1))
    }

    @Test
    fun testMultipleRoutesAddedSimultaneously() {
        val before = stateWith(tabStack = listOf(root))
        val after  = stateWith(tabStack = listOf(root, route1, route2))
        val added = after.getAllRoutes() - before.getAllRoutes()
        assertEquals(setOf<Route>(route1, route2), added)
    }

    // ── handleNavigationSideEffects smoke test ───────────────────────────────
    // The actual Koin scope close is a no-op in commonTest (expect/actual).
    // These verify no exception is thrown for typical state transitions.

    @Test
    fun testHandleSideEffectsDoesNotThrowOnPush() {
        val before = stateWith(tabStack = listOf(root))
        val after  = stateWith(tabStack = listOf(root, route1))
        NavigationEffects.handleNavigationSideEffects(before, after) // must not throw
    }

    @Test
    fun testHandleSideEffectsDoesNotThrowOnPop() {
        val before = stateWith(tabStack = listOf(root, route1))
        val after  = stateWith(tabStack = listOf(root))
        NavigationEffects.handleNavigationSideEffects(before, after) // must not throw
    }

    @Test
    fun testHandleSideEffectsDoesNotThrowWhenUnchanged() {
        val state = stateWith(tabStack = listOf(root, route1))
        NavigationEffects.handleNavigationSideEffects(state, state) // must not throw
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private data class TestRoute(
        override val key: String,
        override val isRootRoute: Boolean
    ) : StackRoute()



    private fun stateWith(
        tabStack: List<Route>,
        modals: List<ModalRoute> = emptyList()
    ): NavigationState {
        val tabDef = TabDefinition(
            id = "tab1",
            label = io.umain.munchies.core.localization.StringResources.app_title,
            icon = io.umain.munchies.core.ui.IconId.Logo,
            rootRoute = root
        )
        return NavigationState(
            tabNavigation = TabNavigationState(
                tabDefinitions = listOf(tabDef),
                activeTabId = "tab1",
                stacksByTab = mapOf("tab1" to tabStack)
            ),
            modalStack = modals
        )
    }

    @Suppress("SameParameterValue")
    private fun makeTabNav(
        stacks: Map<String, List<Route>>,
        active: String
    ): TabNavigationState {
        return TabNavigationState(
            tabDefinitions = stacks.keys.map { id ->
                TabDefinition(
                    id = id,
                    label = io.umain.munchies.core.localization.StringResources.app_title,
                    icon = io.umain.munchies.core.ui.IconId.Logo,
                    rootRoute = root
                )
            },
            activeTabId = active,
            stacksByTab = stacks
        )
    }
}
