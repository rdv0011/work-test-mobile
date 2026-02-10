package io.umain.munchies.android.features.restaurant.di

import io.umain.munchies.feature.restaurant.presentation.RestaurantListViewModel
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import io.umain.munchies.feature.restaurant.di.RestaurantListScope

object RestaurantListScopeFactory {
    fun createScope(): Scope {
        val koin = GlobalContext.get()
        val scopeId = RestaurantListScope.value
        
        return koin.getScopeOrNull(scopeId)
            ?: koin.createScope(
                scopeId = scopeId,
                qualifier = named(RestaurantListScope.qualifierName)
            )
    }
}
