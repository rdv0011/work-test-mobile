package io.umain.munchies.android.features.restaurant.di

import io.umain.munchies.android.features.restaurant.navigation.restaurantDetailRouteHandlerAndroid
import io.umain.munchies.android.features.restaurant.presentation.restaurantdetail.RestaurantDetailAndroidViewModel
import io.umain.munchies.android.features.restaurant.presentation.restaurantlist.RestaurantListAndroidViewModel
import io.umain.munchies.core.lifecycle.Closeable
import io.umain.munchies.feature.restaurant.di.RestaurantDetailScope
import io.umain.munchies.feature.restaurant.di.RestaurantListScope
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import io.umain.munchies.feature.restaurant.presentation.RestaurantListViewModel
import io.umain.munchies.navigation.RouteHandler
import org.koin.core.context.loadKoinModules
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

fun registerAndroidUIWrappersModule() {
    val androidUIWrappersModule = module {
        // The Koin scope (opened by NavigationReducer on push, closed on pop) is the lifecycle
        // owner. No ViewModelStore involved — instances survive within the scope lifetime and are
        // released when the scope is closed. remember(entry.scopeId) in RouteRenderer caches the
        // lookup across recompositions and AnimatedContent transitions.
        scope(named(RestaurantListScope.qualifierName)) {
            scoped {
                RestaurantListAndroidViewModel(get<RestaurantListViewModel>())
            } bind Closeable::class
        }
        scope(named(RestaurantDetailScope.qualifierName)) {
            scoped { (sharedViewModel: RestaurantDetailViewModel) ->
                RestaurantDetailAndroidViewModel(sharedViewModel)
            } bind Closeable::class
        }
        single { restaurantDetailRouteHandlerAndroid() } bind RouteHandler::class
    }
    loadKoinModules(androidUIWrappersModule)
}
