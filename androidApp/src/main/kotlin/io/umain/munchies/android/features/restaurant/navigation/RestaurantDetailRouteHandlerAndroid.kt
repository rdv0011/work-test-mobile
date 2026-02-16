package io.umain.munchies.android.features.restaurant.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.umain.munchies.feature.restaurant.navigation.RestaurantDetailRouteHandler
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.RouteComposableBuilder
import io.umain.munchies.navigation.RouteNavigationMapper
import io.umain.munchies.navigation.ScopedRouteHandler
import io.umain.munchies.android.features.restaurant.presentation.restaurantdetail.RestaurantDetailScreen
import io.umain.munchies.feature.restaurant.di.RestaurantDetailScope
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope

class RestaurantDetailRouteHandlerAndroid(
    private val commonHandler: RestaurantDetailRouteHandler = RestaurantDetailRouteHandler
) : ScopedRouteHandler, RouteComposableBuilder, RouteNavigationMapper {
    
    override val route: Route = commonHandler.route
    
    override fun toRouteString(): String = commonHandler.toRouteString()
    
    override fun canHandle(destination: Destination): Boolean = 
        commonHandler.canHandle(destination)
    
    override fun destinationToRoute(destination: Destination): Route? =
        commonHandler.destinationToRoute(destination)
    
    override fun createScope(route: Route): Scope {
        require(route is RestaurantDetailRoute) { "Expected RestaurantDetailRoute, got $route" }
        val koin = GlobalContext.get()
        val scopeId = route.key
        return koin.getScopeOrNull(scopeId)
            ?: koin.createScope(
                scopeId = scopeId,
                qualifier = named(RestaurantDetailScope("").qualifierName)
            ).also { scope ->
                scope.get<RestaurantDetailViewModel>(
                    parameters = { parametersOf(route.restaurantId) }
                )
            }
    }
    
    override fun buildComposable(
        navGraphBuilder: NavGraphBuilder,
        coordinator: AppCoordinator
    ) {
        navGraphBuilder.composable(
            route = toRouteString(),
            arguments = listOf(
                navArgument("restaurantId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: ""
            RestaurantDetailScreen(
                restaurantId,
                coordinator
            )
        }
    }

    override fun mapDestinationToNavRoute(destination: Destination): String? {
        return (destination as? Destination.RestaurantDetail)?.let { detail ->
            "${toRouteString().substringBefore("/")}/${detail.restaurantId}"
        }
    }

    override fun getRouteCleanupPattern(): String? = toRouteString().substringBefore("/")
    
    override fun getRouteKeyPattern(): String? = RestaurantDetailRoute.KEY_PREFIX
}

fun restaurantDetailRouteHandlerAndroid(): RestaurantDetailRouteHandlerAndroid =
    RestaurantDetailRouteHandlerAndroid()
