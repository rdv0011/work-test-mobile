package io.umain.munchies.feature.restaurant.di

import io.ktor.client.HttpClient
import io.umain.munchies.feature.restaurant.data.remote.KtorRestaurantApi
import io.umain.munchies.feature.restaurant.data.repository.RestaurantRepositoryImpl
import io.umain.munchies.feature.restaurant.domain.repository.RestaurantRepository
import org.koin.dsl.module

val featureRestaurantModule = module {
    single<RestaurantRepository> {
        // Resolve the shared HttpClient from Koin
        val client: HttpClient = get()
        val baseUrl = "https://food-delivery.umain.io"
        val api = KtorRestaurantApi(client, baseUrl)
        RestaurantRepositoryImpl(api)
    }
    // Shared ViewModels (platform-agnostic)
    single { io.umain.munchies.feature.restaurant.presentation.RestaurantListViewModel(get()) }
    factory { io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel(get()) }

}
