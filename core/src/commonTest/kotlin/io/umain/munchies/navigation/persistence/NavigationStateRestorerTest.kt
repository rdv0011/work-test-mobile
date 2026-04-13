package io.umain.munchies.navigation.persistence

import io.umain.munchies.navigation.NavigationState
import io.umain.munchies.navigation.NavigationStateSnapshot
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.SettingsRoute
import io.umain.munchies.navigation.TabDefinition
import io.umain.munchies.navigation.TabDefinitionSnapshot
import io.umain.munchies.navigation.TabNavigationState
import io.umain.munchies.navigation.TabNavigationStateSnapshot
import io.umain.munchies.core.localization.StringResources
import io.umain.munchies.core.ui.IconId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NavigationStateRestorerTest {

    private fun buildSnapshot(
        activeTabId: String = "restaurants",
        tabDefinitions: List<TabDefinitionSnapshot> = listOf(
            TabDefinitionSnapshot("restaurants", StringResources.tab_restaurants, IconId.Restaurant, RestaurantListRoute()),
            TabDefinitionSnapshot("settings", StringResources.tab_settings, IconId.Settings, SettingsRoute())
        ),
        stacksByTab: Map<String, List<io.umain.munchies.navigation.Route>> = mapOf(
            "restaurants" to listOf(RestaurantListRoute()),
            "settings" to listOf(SettingsRoute())
        )
    ): NavigationStateSnapshot = NavigationStateSnapshot(
        tabNavigation = TabNavigationStateSnapshot(
            tabDefinitions = tabDefinitions,
            activeTabId = activeTabId,
            stacksByTab = stacksByTab
        )
    )

    private fun buildDefaultState(): NavigationState {
        val tabNav = TabNavigationState(
            tabDefinitions = listOf(
                TabDefinition("restaurants", StringResources.tab_restaurants, IconId.Restaurant, RestaurantListRoute()),
                TabDefinition("settings", StringResources.tab_settings, IconId.Settings, SettingsRoute())
            ),
            activeTabId = "restaurants",
            stacksByTab = mapOf(
                "restaurants" to listOf(RestaurantListRoute()),
                "settings" to listOf(SettingsRoute())
            )
        )
        return NavigationState(tabNavigation = tabNav)
    }

    @Test
    fun testSuccessfulRestoreReturnsRestoredState() = runTest {
        val snapshot = buildSnapshot()
        val store = FakeNavigationPersistenceStore(loadResult = Result.success(snapshot))
        val restorer = NavigationStateRestorer(store)

        val result = restorer.restoreNavigationState()

        assertEquals("restaurants", result.tabNavigation.activeTabId)
        assertEquals(2, result.tabNavigation.tabDefinitions.size)
    }

    @Test
    fun testNoPersistedStateReturnsDefaultState() = runTest {
        val store = FakeNavigationPersistenceStore(loadResult = Result.success(null))
        val restorer = NavigationStateRestorer(store)

        val result = restorer.restoreNavigationState()

        val expected = buildDefaultState()
        assertEquals(expected.tabNavigation.activeTabId, result.tabNavigation.activeTabId)
        assertEquals(expected.tabNavigation.tabDefinitions.size, result.tabNavigation.tabDefinitions.size)
    }

    @Test
    fun testCorruptedDataReturnsDefaultState() = runTest {
        val store = FakeNavigationPersistenceStore(
            loadResult = Result.failure(RuntimeException("JSON parse error"))
        )
        val restorer = NavigationStateRestorer(store)

        val result = restorer.restoreNavigationState()

        val expected = buildDefaultState()
        assertEquals(expected.tabNavigation.activeTabId, result.tabNavigation.activeTabId)
    }

    @Test
    fun testInvalidSnapshotEmptyTabDefinitionsReturnsDefaultState() = runTest {
        val snapshot = buildSnapshot(tabDefinitions = emptyList(), stacksByTab = emptyMap())
        val store = FakeNavigationPersistenceStore(loadResult = Result.success(snapshot))
        val restorer = NavigationStateRestorer(store)

        val result = restorer.restoreNavigationState()

        val expected = buildDefaultState()
        assertEquals(expected.tabNavigation.activeTabId, result.tabNavigation.activeTabId)
    }

    @Test
    fun testInvalidSnapshotActiveTabNotInDefinitionsReturnsDefaultState() = runTest {
        val snapshot = buildSnapshot(activeTabId = "nonexistent_tab")
        val store = FakeNavigationPersistenceStore(loadResult = Result.success(snapshot))
        val restorer = NavigationStateRestorer(store)

        val result = restorer.restoreNavigationState()

        val expected = buildDefaultState()
        assertEquals(expected.tabNavigation.activeTabId, result.tabNavigation.activeTabId)
    }

    @Test
    fun testInvalidSnapshotEmptyStacksReturnsDefaultState() = runTest {
        val snapshot = buildSnapshot(
            stacksByTab = mapOf(
                "restaurants" to emptyList(),
                "settings" to listOf(SettingsRoute())
            )
        )
        val store = FakeNavigationPersistenceStore(loadResult = Result.success(snapshot))
        val restorer = NavigationStateRestorer(store)

        val result = restorer.restoreNavigationState()

        val expected = buildDefaultState()
        assertEquals(expected.tabNavigation.activeTabId, result.tabNavigation.activeTabId)
    }

    @Test
    fun testPersistenceIOErrorReturnsDefaultState() = runTest {
        val store = FakeNavigationPersistenceStore(
            loadResult = Result.failure(RuntimeException("Disk I/O error"))
        )
        val restorer = NavigationStateRestorer(store)

        val result = restorer.restoreNavigationState()

        assertNotNull(result)
        val expected = buildDefaultState()
        assertEquals(expected.tabNavigation.activeTabId, result.tabNavigation.activeTabId)
        assertEquals(expected.tabNavigation.tabDefinitions.size, result.tabNavigation.tabDefinitions.size)
    }
}

