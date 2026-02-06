package io.umain.munchies.android.features.restaurant.di

import io.umain.munchies.android.features.restaurant.presentation.restaurantlist.RestaurantListAndroidViewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

fun registerAndroidUIWrappersModule() {
    val androidUIWrappersModule = module {
        factory {
            RestaurantListAndroidViewModel(get())
        }
    }
    loadKoinModules(androidUIWrappersModule)
}