package io.umain.munchies.feature.restaurant.di

import io.umain.munchies.feature.restaurant.navigation.RestaurantNavigationViewModel
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import io.umain.munchies.feature.restaurant.presentation.RestaurantListViewModel
import org.koin.core.scope.Scope
import org.koin.core.parameter.parametersOf

/**
 * Type-safe iOS ViewModel retrievers from Scope.
 *
 * Swift cannot call generic functions directly, so we provide explicit, non-generic
 * functions for each ViewModel type. These are called from Swift code.
 *
 * Each function is stable and will be available to Swift during KMP compilation.
 */

fun Scope.getRestaurantListViewModel(): RestaurantListViewModel =
    get()

fun Scope.getRestaurantDetailViewModel(restaurantId: String): RestaurantDetailViewModel =
    get(parameters = { parametersOf(restaurantId) })

fun Scope.getRestaurantNavigationViewModel(): RestaurantNavigationViewModel =
    get()
