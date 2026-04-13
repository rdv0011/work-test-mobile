package io.umain.munchies.navigation.persistence

import io.umain.munchies.navigation.NavigationStateSnapshot
import io.umain.munchies.navigation.persistence.navigationJson
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import platform.Foundation.NSUserDefaults

class IosUserDefaultsPersistence(
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
) : NavigationPersistenceStore {

    companion object {
        private const val KEY = "navigation_state"
    }

    override suspend fun saveNavigationState(snapshot: NavigationStateSnapshot): Result<Unit> =
        try {
            val json = navigationJson.encodeToString(snapshot)
            userDefaults.setObject(json, KEY)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun loadNavigationState(): Result<NavigationStateSnapshot?> =
        try {
            val json = userDefaults.stringForKey(KEY)
            val snapshot = json?.let { navigationJson.decodeFromString<NavigationStateSnapshot>(it) }
            Result.success(snapshot)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun clearNavigationState(): Result<Unit> =
        try {
            userDefaults.removeObjectForKey(KEY)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun hasPersistedState(): Result<Boolean> =
        try {
            val exists = userDefaults.stringForKey(KEY) != null
            Result.success(exists)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
