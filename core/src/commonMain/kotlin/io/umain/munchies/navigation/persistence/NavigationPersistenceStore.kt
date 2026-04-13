package io.umain.munchies.navigation.persistence

import io.umain.munchies.navigation.NavigationStateSnapshot

interface NavigationPersistenceStore {
    suspend fun saveNavigationState(snapshot: NavigationStateSnapshot): Result<Unit>
    suspend fun loadNavigationState(): Result<NavigationStateSnapshot?>
    suspend fun clearNavigationState(): Result<Unit>
    suspend fun hasPersistedState(): Result<Boolean>
}

class NoOpNavigationPersistenceStore : NavigationPersistenceStore {
    override suspend fun saveNavigationState(snapshot: NavigationStateSnapshot) = Result.success(Unit)
    override suspend fun loadNavigationState() = Result.success(null)
    override suspend fun clearNavigationState() = Result.success(Unit)
    override suspend fun hasPersistedState() = Result.success(false)
}
