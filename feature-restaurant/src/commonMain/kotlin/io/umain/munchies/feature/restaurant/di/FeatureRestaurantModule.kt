package io.umain.munchies.feature.restaurant.di

import io.ktor.client.HttpClient
import io.umain.munchies.feature.restaurant.data.remote.KtorRestaurantApi
import io.umain.munchies.feature.restaurant.data.repository.RestaurantRepositoryImpl
import io.umain.munchies.feature.restaurant.domain.repository.RestaurantRepository
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import io.umain.munchies.feature.restaurant.presentation.RestaurantListViewModel
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

    // Shared ViewModels (platform-agnostic)
    factory { RestaurantListViewModel(get()) }

    scope(named(RestaurantDetailScope("").qualifierName)) {
        scoped { (restaurantId: String) ->
            RestaurantDetailViewModel(
                restaurantId = restaurantId,
                repository = get()
            )
        }
    }
}
