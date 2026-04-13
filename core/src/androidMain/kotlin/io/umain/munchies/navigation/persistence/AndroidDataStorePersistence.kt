package io.umain.munchies.navigation.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.umain.munchies.navigation.NavigationStateSnapshot
import io.umain.munchies.navigation.persistence.navigationJson
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

class AndroidDataStorePersistence(
    private val dataStore: DataStore<Preferences>
) : NavigationPersistenceStore {

    companion object {
        private val KEY = stringPreferencesKey("navigation_state")
    }

    override suspend fun saveNavigationState(snapshot: NavigationStateSnapshot): Result<Unit> =
        try {
            val json = navigationJson.encodeToString(snapshot)
            dataStore.edit { preferences -> preferences[KEY] = json }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun loadNavigationState(): Result<NavigationStateSnapshot?> =
        try {
            val json = dataStore.data.first()[KEY]
            val snapshot = json?.let { navigationJson.decodeFromString<NavigationStateSnapshot>(it) }
            Result.success(snapshot)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun clearNavigationState(): Result<Unit> =
        try {
            dataStore.edit { preferences -> preferences.remove(KEY) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun hasPersistedState(): Result<Boolean> =
        try {
            val exists = dataStore.data.first()[KEY] != null
            Result.success(exists)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
