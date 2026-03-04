package io.umain.munchies.feature.restaurant.di

import io.ktor.client.HttpClient
import io.umain.munchies.core.navigation.NavigationDispatcher
import io.umain.munchies.feature.restaurant.data.remote.KtorRestaurantApi
import io.umain.munchies.feature.restaurant.data.repository.RestaurantRepositoryImpl
import io.umain.munchies.feature.restaurant.domain.repository.RestaurantRepository
import io.umain.munchies.feature.restaurant.navigation.RestaurantNavigationViewModel
import io.umain.munchies.feature.restaurant.navigation.ios.RestaurantDetailRouteHandlerImpl
import io.umain.munchies.feature.restaurant.navigation.ios.RestaurantListRouteHandlerImpl
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import io.umain.munchies.feature.restaurant.presentation.RestaurantListViewModel
import io.umain.munchies.navigation.RouteHandler
import io.umain.munchies.logging.logInfo
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.core.qualifier.named

val featureRestaurantModuleIos = module {
    logInfo("FeatureRestaurantModuleIos", "🔧 Building iOS-specific restaurant module")
    
    single<RestaurantRepository> {
        val client: HttpClient = get()
        val baseUrl = "https://food-delivery.umain.io"
        val api = KtorRestaurantApi(client, baseUrl)
        RestaurantRepositoryImpl(api)
    }

    single { RestaurantNavigationViewModel(get<NavigationDispatcher>()) }

    // Register iOS-specific route handlers (bind to RouteHandler so getAll<RouteHandler>() finds them all)
    single { 
        logInfo("FeatureRestaurantModuleIos", "📝 Registering RestaurantListRouteHandlerImpl")
        RestaurantListRouteHandlerImpl 
    } bind RouteHandler::class
    single { 
        logInfo("FeatureRestaurantModuleIos", "📝 Registering RestaurantDetailRouteHandlerImpl")
        RestaurantDetailRouteHandlerImpl 
    } bind RouteHandler::class

    scope(named(RestaurantListScope.qualifierName)) {
        scoped { RestaurantListViewModel(get()) }
    }

    scope(named(RestaurantDetailScope("").qualifierName)) {
        scoped { (restaurantId: String) ->
            RestaurantDetailViewModel(
                restaurantId = restaurantId,
                repository = get(),
                navigationViewModel = get()
            )
        }
    }
    
    logInfo("FeatureRestaurantModuleIos", "✅ iOS-specific restaurant module built")
}

