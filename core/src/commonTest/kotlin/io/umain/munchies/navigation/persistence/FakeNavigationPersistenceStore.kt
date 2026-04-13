package io.umain.munchies.navigation.persistence

import io.umain.munchies.navigation.NavigationStateSnapshot

class FakeNavigationPersistenceStore(
    private val loadResult: Result<NavigationStateSnapshot?> = Result.success(null)
) : NavigationPersistenceStore {

    private var savedSnapshot: NavigationStateSnapshot? = null

    override suspend fun saveNavigationState(snapshot: NavigationStateSnapshot): Result<Unit> {
        savedSnapshot = snapshot
        return Result.success(Unit)
    }

    override suspend fun loadNavigationState(): Result<NavigationStateSnapshot?> =
        if (loadResult.isSuccess && loadResult.getOrNull() == null && savedSnapshot != null) {
            Result.success(savedSnapshot)
        } else {
            loadResult
        }

    override suspend fun clearNavigationState(): Result<Unit> {
        savedSnapshot = null
        return Result.success(Unit)
    }

    override suspend fun hasPersistedState(): Result<Boolean> {
        if (loadResult.isFailure) return Result.success(false)
        return Result.success(loadResult.getOrNull() != null || savedSnapshot != null)
    }
}
