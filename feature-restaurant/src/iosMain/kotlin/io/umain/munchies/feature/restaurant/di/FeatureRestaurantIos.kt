package io.umain.munchies.feature.restaurant.di

import io.umain.munchies.core.di.KmpScopeId
import io.umain.munchies.core.viewmodel.ScopedViewModelHandle
import io.umain.munchies.core.viewmodel.scopedViewModel
import io.umain.munchies.feature.restaurant.presentation.RestaurantListViewModel
import io.umain.munchies.di.getKoin
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel

/**
 * iOS helpers to retrieve shared ViewModel instances from Koin.
 * These functions provide stable symbols for Swift to call.
 */
fun getRestaurantListViewModelIos(): RestaurantListViewModel {
    return getKoin().get()
}

fun getRestaurantDetailViewModel(
    scopeId: KmpScopeId,
    restaurantId: String
): ScopedViewModelHandle<RestaurantDetailViewModel> {
    return scopedViewModel(
        vmClass = RestaurantDetailViewModel::class,
        scopeId = scopeId,
        params = listOf(restaurantId)
    )
}