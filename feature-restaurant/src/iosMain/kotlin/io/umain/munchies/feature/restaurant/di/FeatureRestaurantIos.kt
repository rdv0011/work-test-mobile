package io.umain.munchies.feature.restaurant.di

import io.umain.munchies.di.getKoin
import io.umain.munchies.feature.restaurant.navigation.ios.RestaurantDetailRouteHandlerImpl
import io.umain.munchies.feature.restaurant.navigation.ios.RestaurantListRouteHandlerImpl
import io.umain.munchies.feature.restaurant.presentation.RestaurantListViewModel
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import io.umain.munchies.feature.restaurant.navigation.RestaurantNavigationViewModel
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantListUiState
import io.umain.munchies.feature.restaurant.presentation.state.RestaurantDetailUiState
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.RestaurantListRoute
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.core.parameter.parametersOf

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

fun createRestaurantListRoute(): RestaurantListRoute = RestaurantListRoute()

fun createRestaurantDetailRoute(restaurantId: String): RestaurantDetailRoute =
    RestaurantDetailRoute(restaurantId)

fun getRestaurantListRouteHandler() = RestaurantListRouteHandlerImpl

fun getRestaurantDetailRouteHandler() = RestaurantDetailRouteHandlerImpl

// ViewModel export functions (for framework public API)
/**
 * Get the RestaurantListViewModel from the scope.
 * Must be called after the scope is created via createRestaurantListScopeIos().
 */
fun getRestaurantListViewModelIos(): RestaurantListViewModel {
    val koin = getKoin()
    val scope = koin.getScope(RestaurantListScope.value)
    return scope.get<RestaurantListViewModel>()
}

/**
 * Get the RestaurantDetailViewModel from the scope.
 * Must be called after the scope is created via createRestaurantDetailScopeIos(restaurantId).
 */
fun getRestaurantDetailViewModelIos(restaurantId: String): RestaurantDetailViewModel {
    val koin = getKoin()
    val scopeId = RestaurantDetailScope(restaurantId).value
    val scope = koin.getScope(scopeId)
    return scope.get<RestaurantDetailViewModel> { parametersOf(restaurantId) }
}

/**
 * Get the RestaurantNavigationViewModel for restaurant feature.
 */
fun getRestaurantNavigationViewModelIos(): RestaurantNavigationViewModel {
    val koin = getKoin()
    return koin.get<RestaurantNavigationViewModel>()
}
