package io.umain.munchies.android.features.restaurant.di

import io.umain.munchies.android.features.restaurant.presentation.restaurantdetail.RestaurantDetailAndroidViewModel
import io.umain.munchies.android.features.restaurant.presentation.restaurantlist.RestaurantListAndroidViewModel
import io.umain.munchies.feature.restaurant.navigation.android.restaurantDetailRouteHandlerAndroid
import io.umain.munchies.feature.restaurant.navigation.android.restaurantListRouteHandlerAndroid
import io.umain.munchies.core.lifecycle.Closeable
import io.umain.munchies.feature.restaurant.di.RestaurantDetailScope
import io.umain.munchies.feature.restaurant.di.RestaurantListScope
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import io.umain.munchies.feature.restaurant.presentation.RestaurantListViewModel
import io.umain.munchies.navigation.RouteHandler
import org.koin.core.context.loadKoinModules
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

fun registerAndroidUIWrappersModule() {
    val androidUIWrappersModule = module {
        scope(named(RestaurantListScope.qualifierName)) {
            scoped {
                RestaurantListAndroidViewModel(get<RestaurantListViewModel>())
            } bind Closeable::class
        }
        scope(named(RestaurantDetailScope.qualifierName)) {
            scoped {
                val restaurantId = RestaurantDetailScope.fromScopeId(this.id)
                RestaurantDetailAndroidViewModel(
                    get<RestaurantDetailViewModel>(parameters = { parametersOf(restaurantId) })
                )
            } bind Closeable::class
        }
        single { restaurantDetailRouteHandlerAndroid() } bind RouteHandler::class
        single { restaurantListRouteHandlerAndroid() } bind RouteHandler::class
    }
    loadKoinModules(androidUIWrappersModule)
}
