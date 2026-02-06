package io.umain.munchies.feature.restaurant.di

import io.umain.munchies.feature.restaurant.presentation.RestaurantListViewModel
import io.umain.munchies.di.getKoin

/**
 * iOS helpers to retrieve shared ViewModel instances from Koin.
 * These functions provide stable symbols for Swift to call.
 */
fun getRestaurantListViewModelIos(): RestaurantListViewModel {
    return getKoin().get()
}
