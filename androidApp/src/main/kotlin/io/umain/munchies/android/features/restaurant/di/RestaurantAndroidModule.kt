package io.umain.munchies.android.features.restaurant.di

import io.umain.munchies.android.features.restaurant.presentation.restaurantdetail.RestaurantDetailAndroidViewModel
import io.umain.munchies.android.features.restaurant.presentation.restaurantlist.RestaurantListAndroidViewModel
import io.umain.munchies.feature.restaurant.di.RestaurantDetailScope
import io.umain.munchies.feature.restaurant.di.RestaurantListScope
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import io.umain.munchies.feature.restaurant.presentation.RestaurantListViewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun registerAndroidUIWrappersModule() {
    val androidUIWrappersModule = module {
        scope(named(RestaurantListScope.qualifierName)) {
            scoped {
                RestaurantListAndroidViewModel(get<RestaurantListViewModel>())
            }
        }
        // Add RestaurantDetail scope for Android ViewModel
        scope(named(RestaurantDetailScope("").qualifierName)) {
            scoped { (restaurantId: String) ->
                RestaurantDetailAndroidViewModel(get<RestaurantDetailViewModel>(parameters = { org.koin.core.parameter.parametersOf(restaurantId) }))
            }
        }
    }
    loadKoinModules(androidUIWrappersModule)
}