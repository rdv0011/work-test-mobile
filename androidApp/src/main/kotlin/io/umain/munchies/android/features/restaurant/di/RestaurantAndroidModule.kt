package io.umain.munchies.android.features.restaurant.di

import io.umain.munchies.android.features.restaurant.presentation.restaurantlist.RestaurantListAndroidViewModel
import io.umain.munchies.feature.restaurant.presentation.RestaurantListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

fun registerAndroidUIWrappersModule() {
    val androidUIWrappersModule = module {
        viewModel {
            RestaurantListAndroidViewModel(get())
        }
        factory { RestaurantListViewModel(repository = get()) }
    }
    loadKoinModules(androidUIWrappersModule)
}