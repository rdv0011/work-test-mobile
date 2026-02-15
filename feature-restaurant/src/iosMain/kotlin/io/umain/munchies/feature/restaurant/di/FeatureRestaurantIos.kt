package io.umain.munchies.feature.restaurant.di

import io.umain.munchies.di.getKoin
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

/**
 * iOS helpers to retrieve shared ViewModel instances from Koin.
 * These functions provide stable symbols for Swift to call.
 *
 * OWNERSHIP MODEL:
 * - Factory functions create Koin scopes but do NOT own them
 * - iOS RouteRegistry owns the scopes and manages their lifetime
 * - ViewModelHolders call factories to create/get scopes
 */

/**
 * Factory function for RestaurantListScope.
 * Creates or retrieves the list scope from Koin.
 *
 * CRITICAL: This is a factory only. RouteRegistry decides when the scope lives.
 * Does NOT initialize ViewModels—that happens when the ViewModelHolder calls getViewModel().
 */
fun createRestaurantListScopeIos(): Scope {
    val koin = getKoin()
    val scopeId = RestaurantListScope.value
    
    return koin.getScopeOrNull(scopeId)
        ?: koin.createScope(
            scopeId = scopeId,
            qualifier = named(RestaurantListScope.qualifierName)
        )
}

/**
 * Factory function for RestaurantDetailScope.
 * Creates or retrieves the detail scope from Koin with the given restaurantId.
 *
 * CRITICAL: This is a factory only. RouteRegistry decides when the scope lives.
 * Does NOT initialize ViewModels—that happens when the ViewModelHolder calls getViewModel().
 *
 * @param restaurantId The restaurant ID to use as scope parameter
 * @return A Koin Scope for this restaurant detail
 */
fun createRestaurantDetailScopeIos(restaurantId: String): Scope {
    val koin = getKoin()
    val scopeId = RestaurantDetailScope(restaurantId).value
    
    return koin.getScopeOrNull(scopeId)
        ?: koin.createScope(
            scopeId = scopeId,
            qualifier = named(RestaurantDetailScope("").qualifierName)
        )
}