package io.umain.munchies.android.features.restaurant.di

import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope
import io.umain.munchies.feature.restaurant.di.RestaurantDetailScope

object RestaurantDetailScopeFactory {
    fun createScope(restaurantId: String): Scope {
        val koin = GlobalContext.get()
        val scopeId = RestaurantDetailScope(restaurantId).value
        
        return koin.getScopeOrNull(scopeId)
            ?: koin.createScope(
                scopeId = scopeId,
                qualifier = named(RestaurantDetailScope("").qualifierName)
            ).also { scope ->
                scope.get<RestaurantDetailViewModel>(
                    parameters = { parametersOf(restaurantId) }
                )
            }
    }
}
