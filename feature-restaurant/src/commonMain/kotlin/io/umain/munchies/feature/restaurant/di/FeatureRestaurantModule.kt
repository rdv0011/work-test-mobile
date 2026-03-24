package io.umain.munchies.feature.restaurant.di

import io.ktor.client.HttpClient
import io.umain.munchies.core.navigation.NavigationDispatcher
import io.umain.munchies.feature.restaurant.data.remote.KtorRestaurantApi
import io.umain.munchies.feature.restaurant.data.repository.RestaurantRepositoryImpl
import io.umain.munchies.feature.restaurant.domain.repository.RestaurantRepository
import io.umain.munchies.feature.restaurant.navigation.RestaurantNavigationViewModel
import io.umain.munchies.feature.restaurant.navigation.RestaurantDetailRouteHandler
import io.umain.munchies.feature.restaurant.navigation.RestaurantListRouteHandler
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import io.umain.munchies.feature.restaurant.presentation.RestaurantListViewModel
import io.umain.munchies.navigation.RouteHandler
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.core.qualifier.TypeQualifier
import org.koin.core.qualifier.named

val featureRestaurantModule = module {

    single<RestaurantRepository> {
        // Resolve the shared HttpClient from Koin
        val client: HttpClient = get()
        val baseUrl = "https://food-delivery.umain.io"
        val api = KtorRestaurantApi(client, baseUrl)
        RestaurantRepositoryImpl(api)
    }

    single { RestaurantNavigationViewModel(get<NavigationDispatcher>()) }

    // Register route handlers
    single { RestaurantListRouteHandler } bind RouteHandler::class
    single { RestaurantDetailRouteHandler } bind RouteHandler::class

    // Restaurant List scope (singleton per app session, managed by RouteRegistry)
    scope(named(RestaurantListScope.qualifierName)) {
        scoped { RestaurantListViewModel(get(), get()) }
    }

    // Restaurant Detail scope (parameterized per restaurant, managed by RouteRegistry)
    scope(named(RestaurantDetailScope("").qualifierName)) {
        scoped { (restaurantId: String) ->
            RestaurantDetailViewModel(
                restaurantId = restaurantId,
                repository = get(),
                navigationViewModel = get(),
                stringProvider = get(),
            )
        }
    }
}
