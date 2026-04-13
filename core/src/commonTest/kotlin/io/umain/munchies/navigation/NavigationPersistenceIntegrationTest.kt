package io.umain.munchies.navigation

import io.umain.munchies.core.localization.StringResources
import io.umain.munchies.core.ui.IconId
import io.umain.munchies.navigation.persistence.FakeNavigationPersistenceStore
import io.umain.munchies.navigation.persistence.NavigationStateRestorer
import io.umain.munchies.navigation.persistence.createDefaultNavigationState
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.measureTime

class NavigationPersistenceIntegrationTest {

    private fun buildTabNavigation(
        activeTabId: String = "restaurants",
        restaurantsStack: List<Route> = listOf(RestaurantListRoute()),
        settingsStack: List<Route> = listOf(SettingsRoute())
    ): TabNavigationState = TabNavigationState(
        tabDefinitions = listOf(
            TabDefinition("restaurants", StringResources.tab_restaurants, IconId.Restaurant, RestaurantListRoute()),
            TabDefinition("settings", StringResources.tab_settings, IconId.Settings, SettingsRoute())
        ),
        activeTabId = activeTabId,
        stacksByTab = mapOf(
            "restaurants" to restaurantsStack,
            "settings" to settingsStack
        )
    )

    private fun buildNavigationState(
        activeTabId: String = "restaurants",
        restaurantsStack: List<Route> = listOf(RestaurantListRoute()),
        settingsStack: List<Route> = listOf(SettingsRoute()),
        modalStack: List<ModalRoute> = emptyList(),
        originDeepLink: String? = null
    ): NavigationState = NavigationState(
        tabNavigation = buildTabNavigation(activeTabId, restaurantsStack, settingsStack),
        modalStack = modalStack,
        originDeepLink = originDeepLink
    )

    @Test
    fun testHappyPathFullSaveRestoreRoundtrip() = runTest {
        val state = buildNavigationState(
            activeTabId = "settings",
            restaurantsStack = listOf(RestaurantListRoute(), RestaurantDetailRoute("restaurant-42")),
            settingsStack = listOf(SettingsRoute())
        )

        val store = FakeNavigationPersistenceStore()
        store.saveNavigationState(state.toSnapshot(restoredFromCrash = false))

        val restoredState = NavigationStateRestorer(store).restoreNavigationState()

        assertEquals("settings", restoredState.tabNavigation.activeTabId)
        assertEquals(2, restoredState.tabNavigation.tabDefinitions.size)

        val restaurantsStack = requireNotNull(restoredState.tabNavigation.stacksByTab["restaurants"]) {
            "Expected restaurants stack in restored state"
        }
        assertEquals(2, restaurantsStack.size)
        assertEquals(RestaurantListRoute().key, restaurantsStack[0].key)
        assertTrue(restaurantsStack[1] is RestaurantDetailRoute)
        assertEquals("restaurant-42", (restaurantsStack[1] as RestaurantDetailRoute).restaurantId)

        val settingsStack = requireNotNull(restoredState.tabNavigation.stacksByTab["settings"]) {
            "Expected settings stack in restored state"
        }
        assertEquals(1, settingsStack.size)
    }

    @Test
    fun testCrashSimulationPersistsAndRestoresLastState() = runTest {
        val store = FakeNavigationPersistenceStore()
        val stateBeforeCrash = buildNavigationState(
            activeTabId = "restaurants",
            restaurantsStack = listOf(RestaurantListRoute(), RestaurantDetailRoute("crash-test-99"))
        )
        store.saveNavigationState(stateBeforeCrash.toSnapshot(restoredFromCrash = false))

        assertTrue(store.hasPersistedState().getOrThrow())

        val restoredAfterCrash = NavigationStateRestorer(store).restoreNavigationState()

        assertEquals("restaurants", restoredAfterCrash.tabNavigation.activeTabId)
        val stack = requireNotNull(restoredAfterCrash.tabNavigation.stacksByTab["restaurants"]) {
            "Expected restaurants stack after crash restoration"
        }
        assertEquals(2, stack.size)
        assertTrue(stack[1] is RestaurantDetailRoute)
        assertEquals("crash-test-99", (stack[1] as RestaurantDetailRoute).restaurantId)
    }

    @Test
    fun testPersistedSnapshotStructureRemainsCompact() = runTest {
        val deepState = buildNavigationState(
            activeTabId = "restaurants",
            restaurantsStack = listOf(
                RestaurantListRoute(),
                RestaurantDetailRoute("r-001"),
                RestaurantDetailRoute("r-002"),
                RestaurantDetailRoute("r-003")
            ),
            modalStack = listOf(FilterModalRoute(listOf("fast-food", "vegetarian", "sushi")))
        )
        val deepSnapshot = deepState.toSnapshot()

        val totalRouteCount = deepSnapshot.tabNavigation.stacksByTab.values.sumOf { it.size } +
            deepSnapshot.modalStack.size
        val tabCount = deepSnapshot.tabNavigation.tabDefinitions.size

        assertTrue(tabCount <= 10, "Tab count ($tabCount) should stay small for typical usage")
        assertTrue(totalRouteCount <= 100, "Total route count ($totalRouteCount) should stay reasonable")

        val simpleSnapshot = buildNavigationState().toSnapshot()
        val simpleRouteCount = simpleSnapshot.tabNavigation.stacksByTab.values.sumOf { it.size } +
            simpleSnapshot.modalStack.size
        assertTrue(
            simpleRouteCount <= 10,
            "Simple navigation snapshot should have very few routes; got $simpleRouteCount"
        )
    }

    @Test
    fun testDeepLinkTakesPriorityOverRestoredState() = runTest {
        val store = FakeNavigationPersistenceStore()
        store.saveNavigationState(buildNavigationState(activeTabId = "settings").toSnapshot())

        val restoredState = NavigationStateRestorer(store).restoreNavigationState()
        assertEquals("settings", restoredState.tabNavigation.activeTabId)

        val deepLinkState = buildNavigationState(
            activeTabId = "restaurants",
            restaurantsStack = listOf(RestaurantListRoute(), RestaurantDetailRoute("deep-linked-restaurant")),
            originDeepLink = "munchies://restaurants/deep-linked-restaurant"
        )
        val coordinator = AppCoordinator(initialState = restoredState)
        coordinator.applyNavigationState(deepLinkState, clearCurrentStack = true)

        val finalState = coordinator.getCurrentState()
        assertEquals("restaurants", finalState.tabNavigation.activeTabId)
        assertEquals("munchies://restaurants/deep-linked-restaurant", finalState.originDeepLink)
        val finalStack = requireNotNull(finalState.tabNavigation.stacksByTab["restaurants"]) {
            "Expected restaurants stack after deep link application"
        }
        assertEquals(2, finalStack.size)
        assertEquals("deep-linked-restaurant", (finalStack[1] as RestaurantDetailRoute).restaurantId)
    }

    @Test
    fun testInvalidSnapshotFallsBackToDefault() = runTest {
        val defaultState = createDefaultNavigationState()

        val failingStore = FakeNavigationPersistenceStore(
            loadResult = Result.failure(RuntimeException("Corrupted data"))
        )
        val restoredA = NavigationStateRestorer(failingStore).restoreNavigationState()
        assertEquals(defaultState.tabNavigation.activeTabId, restoredA.tabNavigation.activeTabId)
        assertEquals(defaultState.tabNavigation.tabDefinitions.size, restoredA.tabNavigation.tabDefinitions.size)

        val emptyTabsStore = FakeNavigationPersistenceStore(
            loadResult = Result.success(
                NavigationStateSnapshot(
                    tabNavigation = TabNavigationStateSnapshot(
                        tabDefinitions = emptyList(),
                        activeTabId = "restaurants",
                        stacksByTab = emptyMap()
                    )
                )
            )
        )
        val restoredB = NavigationStateRestorer(emptyTabsStore).restoreNavigationState()
        assertEquals(defaultState.tabNavigation.activeTabId, restoredB.tabNavigation.activeTabId)

        val wrongActiveTabStore = FakeNavigationPersistenceStore(
            loadResult = Result.success(
                NavigationStateSnapshot(
                    tabNavigation = TabNavigationStateSnapshot(
                        tabDefinitions = listOf(
                            TabDefinitionSnapshot("restaurants", StringResources.tab_restaurants, IconId.Restaurant, RestaurantListRoute()),
                            TabDefinitionSnapshot("settings", StringResources.tab_settings, IconId.Settings, SettingsRoute())
                        ),
                        activeTabId = "ghost_tab",
                        stacksByTab = mapOf(
                            "restaurants" to listOf(RestaurantListRoute()),
                            "settings" to listOf(SettingsRoute())
                        )
                    )
                )
            )
        )
        val restoredC = NavigationStateRestorer(wrongActiveTabStore).restoreNavigationState()
        assertEquals(defaultState.tabNavigation.activeTabId, restoredC.tabNavigation.activeTabId)

        val emptyStackStore = FakeNavigationPersistenceStore(
            loadResult = Result.success(
                NavigationStateSnapshot(
                    tabNavigation = TabNavigationStateSnapshot(
                        tabDefinitions = listOf(
                            TabDefinitionSnapshot("restaurants", StringResources.tab_restaurants, IconId.Restaurant, RestaurantListRoute()),
                            TabDefinitionSnapshot("settings", StringResources.tab_settings, IconId.Settings, SettingsRoute())
                        ),
                        activeTabId = "restaurants",
                        stacksByTab = mapOf(
                            "restaurants" to emptyList(),
                            "settings" to listOf(SettingsRoute())
                        )
                    )
                )
            )
        )
        val restoredD = NavigationStateRestorer(emptyStackStore).restoreNavigationState()
        assertEquals(defaultState.tabNavigation.activeTabId, restoredD.tabNavigation.activeTabId)
    }

    @Test
    fun testSaveAndRestoreCompleteWithin100ms() = runTest {
        val store = FakeNavigationPersistenceStore()
        val state = buildNavigationState(
            activeTabId = "restaurants",
            restaurantsStack = listOf(RestaurantListRoute(), RestaurantDetailRoute("perf-test"))
        )

        val saveTime = measureTime {
            store.saveNavigationState(state.toSnapshot())
        }
        val restoreTime = measureTime {
            NavigationStateRestorer(store).restoreNavigationState()
        }

        assertTrue(
            saveTime.inWholeMilliseconds < 100L,
            "Save took ${saveTime.inWholeMilliseconds}ms, expected < 100ms"
        )
        assertTrue(
            restoreTime.inWholeMilliseconds < 100L,
            "Restore took ${restoreTime.inWholeMilliseconds}ms, expected < 100ms"
        )
    }

    @Test
    fun testClearNavigationStateWipesPersistedData() = runTest {
        val store = FakeNavigationPersistenceStore()
        store.saveNavigationState(buildNavigationState(activeTabId = "settings").toSnapshot())

        assertTrue(store.hasPersistedState().getOrThrow())
        requireNotNull(store.loadNavigationState().getOrNull()) { "Expected snapshot after save" }

        assertTrue(store.clearNavigationState().isSuccess)

        assertFalse(store.hasPersistedState().getOrThrow())
        assertNull(store.loadNavigationState().getOrNull())

        val restoredState = NavigationStateRestorer(store).restoreNavigationState()
        val defaultState = createDefaultNavigationState()
        assertEquals(defaultState.tabNavigation.activeTabId, restoredState.tabNavigation.activeTabId)
        assertEquals(defaultState.tabNavigation.tabDefinitions.size, restoredState.tabNavigation.tabDefinitions.size)
    }
}
