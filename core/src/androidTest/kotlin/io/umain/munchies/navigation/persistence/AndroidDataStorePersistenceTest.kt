package io.umain.munchies.navigation.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import io.umain.munchies.core.ui.IconId
import io.umain.munchies.navigation.NavigationDirection
import io.umain.munchies.navigation.NavigationStateSnapshot
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.SettingsRoute
import io.umain.munchies.navigation.TabDefinitionSnapshot
import io.umain.munchies.navigation.TabNavigationStateSnapshot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidDataStorePersistenceTest {

    private lateinit var fakeDataStore: FakeDataStore
    private lateinit var persistence: AndroidDataStorePersistence

    @Before
    fun setUp() {
        fakeDataStore = FakeDataStore()
        persistence = AndroidDataStorePersistence(fakeDataStore)
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
    fun `saveNavigationState and loadNavigationState returns same snapshot`() = runTest {
        val snapshot = sampleSnapshot()

        val saveResult = persistence.saveNavigationState(snapshot)
        assertTrue("save should succeed", saveResult.isSuccess)

        val loadResult = persistence.loadNavigationState()
        assertTrue("load should succeed", loadResult.isSuccess)

        val loaded = loadResult.getOrNull()
        assertNotNull("loaded snapshot must not be null", loaded)
        assertEquals("activeTabId must match", snapshot.tabNavigation.activeTabId, loaded!!.tabNavigation.activeTabId)
        assertEquals("restorationTimestamp must match", snapshot.restorationTimestamp, loaded.restorationTimestamp)
        assertEquals("modalStack size must match", snapshot.modalStack.size, loaded.modalStack.size)
    }

    @Test
    fun `loadNavigationState returns null when no state has been saved`() = runTest {
        val result = persistence.loadNavigationState()

        assertTrue("result should be success", result.isSuccess)
        assertNull("snapshot should be null when nothing was saved", result.getOrNull())
    }

    @Test
    fun `clearNavigationState removes persisted state`() = runTest {
        persistence.saveNavigationState(sampleSnapshot())

        val clearResult = persistence.clearNavigationState()
        assertTrue("clear should succeed", clearResult.isSuccess)

        val loadResult = persistence.loadNavigationState()
        assertTrue("load after clear should succeed", loadResult.isSuccess)
        assertNull("snapshot should be null after clear", loadResult.getOrNull())
    }

    @Test
    fun `hasPersistedState returns true after save and false after clear`() = runTest {
        val beforeSave = persistence.hasPersistedState()
        assertTrue("hasPersistedState result should be success", beforeSave.isSuccess)
        assertFalse("should be false before any save", beforeSave.getOrThrow())

        persistence.saveNavigationState(sampleSnapshot())

        val afterSave = persistence.hasPersistedState()
        assertTrue("hasPersistedState result should be success", afterSave.isSuccess)
        assertTrue("should be true after save", afterSave.getOrThrow())

        persistence.clearNavigationState()

        val afterClear = persistence.hasPersistedState()
        assertTrue("hasPersistedState result should be success", afterClear.isSuccess)
        assertFalse("should be false after clear", afterClear.getOrThrow())
    }

    @Test
    fun `loadNavigationState handles serialization error gracefully`() = runTest {
        fakeDataStore.injectRawValue("navigation_state", "{ NOT VALID JSON !!!}")

        val result = persistence.loadNavigationState()

        assertTrue("result should be failure for corrupted data", result.isFailure)
    }

    private class FakeDataStore : DataStore<Preferences> {

        private val state = MutableStateFlow(emptyPreferences())

        override val data: Flow<Preferences> = state

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val updated = transform(state.value)
            state.value = updated
            return updated
        }

        fun injectRawValue(key: String, value: String) {
            val mutable = state.value.toMutablePreferences()
            mutable[androidx.datastore.preferences.core.stringPreferencesKey(key)] = value
            state.value = mutable
        }
    }
}
