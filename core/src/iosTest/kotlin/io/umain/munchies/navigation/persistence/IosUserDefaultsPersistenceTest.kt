package io.umain.munchies.navigation.persistence

import io.umain.munchies.core.ui.IconId
import io.umain.munchies.navigation.NavigationDirection
import io.umain.munchies.navigation.NavigationStateSnapshot
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.SettingsRoute
import io.umain.munchies.navigation.TabDefinitionSnapshot
import io.umain.munchies.navigation.TabNavigationStateSnapshot
import platform.Foundation.NSUserDefaults
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.Ignore
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IosUserDefaultsPersistenceTest {

    private val testSuiteName = "io.umain.munchies.test.NavPersistence"
    private lateinit var userDefaults: NSUserDefaults
    private lateinit var persistence: IosUserDefaultsPersistence

    @BeforeTest
    fun setUp() {
        userDefaults = NSUserDefaults(suiteName = testSuiteName)
        userDefaults.removeSuiteNamed(testSuiteName) // Clear any leftover data from previous test runs
        persistence = IosUserDefaultsPersistence(userDefaults)
    }

    @AfterTest
    fun tearDown() {
        userDefaults.removeSuiteNamed(testSuiteName)
    }

    private fun sampleSnapshot(): NavigationStateSnapshot = NavigationStateSnapshot(
        tabNavigation = TabNavigationStateSnapshot(
            tabDefinitions = listOf(
                TabDefinitionSnapshot("restaurants", "Restaurants", IconId.Restaurant, RestaurantListRoute()),
                TabDefinitionSnapshot("settings", "Settings", IconId.Settings, SettingsRoute())
            ),
            activeTabId = "restaurants",
            stacksByTab = mapOf(
                "restaurants" to listOf(RestaurantListRoute()),
                "settings" to listOf(SettingsRoute())
            ),
            navigationDirection = NavigationDirection.Forward
        ),
        modalStack = emptyList(),
        originDeepLink = null,
        restoredFromCrash = false,
        restorationTimestamp = 1_000_000L
    )

    @Test
    fun saveNavigationStateAndLoadNavigationStateReturnsSameSnapshot() = runBlocking {
        val snapshot = sampleSnapshot()

        val saveResult = persistence.saveNavigationState(snapshot)
        assertTrue(saveResult.isSuccess, "save should succeed")

        val loadResult = persistence.loadNavigationState()
        assertTrue(loadResult.isSuccess, "load should succeed")

        val loaded = loadResult.getOrNull()
        assertNotNull(loaded, "loaded snapshot must not be null")
        assertEquals(snapshot.tabNavigation.activeTabId, loaded.tabNavigation.activeTabId, "activeTabId must match")
        assertEquals(snapshot.restorationTimestamp, loaded.restorationTimestamp, "restorationTimestamp must match")
        assertEquals(snapshot.modalStack.size, loaded.modalStack.size, "modalStack size must match")
    }

    @Ignore
    @Test
    fun loadNavigationStateReturnsNullWhenNoStateHasBeenSaved() = runBlocking {
        // Ensure suite is completely clean before testing
        val cleanDefaults = NSUserDefaults(suiteName = testSuiteName)
        cleanDefaults.removeSuiteNamed(testSuiteName)
        val cleanPersistence = IosUserDefaultsPersistence(cleanDefaults)
        
        val result = cleanPersistence.loadNavigationState()

        assertTrue(result.isSuccess, "result should be success")
        assertNull(result.getOrNull(), "snapshot should be null when nothing was saved")
    }

    @Test
    fun clearNavigationStateRemovesPersistedState() = runBlocking {
        persistence.saveNavigationState(sampleSnapshot())

        val clearResult = persistence.clearNavigationState()
        assertTrue(clearResult.isSuccess, "clear should succeed")

        val loadResult = persistence.loadNavigationState()
        assertTrue(loadResult.isSuccess, "load after clear should succeed")
        assertNull(loadResult.getOrNull(), "snapshot should be null after clear")
    }

    @Test
    fun hasPersistedStateReturnsTrueAfterSaveAndFalseAfterClear() = runBlocking {
        val beforeSave = persistence.hasPersistedState()
        assertTrue(beforeSave.isSuccess, "hasPersistedState result should be success")
        assertFalse(beforeSave.getOrThrow(), "should be false before any save")

        persistence.saveNavigationState(sampleSnapshot())

        val afterSave = persistence.hasPersistedState()
        assertTrue(afterSave.isSuccess, "hasPersistedState result should be success")
        assertTrue(afterSave.getOrThrow(), "should be true after save")

        persistence.clearNavigationState()

        val afterClear = persistence.hasPersistedState()
        assertTrue(afterClear.isSuccess, "hasPersistedState result should be success")
        assertFalse(afterClear.getOrThrow(), "should be false after clear")
    }

    @Test
    fun loadNavigationStateHandlesCorruptedDataGracefully() = runBlocking {
        userDefaults.setObject("{ NOT VALID JSON !!!}", "navigation_state")

        val result = persistence.loadNavigationState()

        assertTrue(result.isFailure, "result should be failure for corrupted data")
    }
}

private fun <T> runBlocking(block: suspend () -> T): T {
    var result: T? = null
    var error: Throwable? = null
    kotlinx.coroutines.runBlocking {
        try {
            result = block()
        } catch (e: Throwable) {
            error = e
        }
    }
    error?.let { throw it }
    @Suppress("UNCHECKED_CAST")
    return result as T
}
