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
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.dsl.bind
import org.koin.dsl.module

fun registerAndroidUIWrappersModule() {
    val androidUIWrappersModule = module {
        // Scopes are created by AppCoordinator (via ScopedRouteHandler.createScope) when routes
        // enter the navigation state, and closed when they leave. The UI only resolves already-open
        // scopes — it never creates or closes them.
        scope(named(RestaurantListScope.qualifierName)) {
            scoped {
                RestaurantListAndroidViewModel(get<RestaurantListViewModel>())
            } bind Closeable::class
        }
        scope(named(RestaurantDetailScope.qualifierName)) {
            scoped {
                // Extract restaurantId from the scope's own id (format: "RestaurantDetail_<id>")
                // so no external parameter is needed when the UI calls scope.get().
                val restaurantId = RestaurantDetailScope.fromScopeId(this.id)
                RestaurantDetailAndroidViewModel(
                    get<RestaurantDetailViewModel>(parameters = { parametersOf(restaurantId) })
                )
            } bind Closeable::class
        }
        single { restaurantDetailRouteHandlerAndroid() } bind RouteHandler::class
        single { io.umain.munchies.android.features.restaurant.navigation.restaurantListRouteHandlerAndroid() } bind RouteHandler::class
    }
    loadKoinModules(androidUIWrappersModule)
}
