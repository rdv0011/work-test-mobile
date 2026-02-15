package io.umain.munchies.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.umain.munchies.android.features.restaurant.presentation.restaurantdetail.RestaurantDetailScreen
import io.umain.munchies.android.features.restaurant.presentation.restaurantlist.RestaurantListScreen
import io.umain.munchies.feature.restaurant.navigation.RestaurantRouteProvider
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.NavigationEvent
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.RouteHandler
import io.umain.munchies.navigation.RouteProvider
import kotlinx.coroutines.flow.collectLatest

val LocalRouteRegistry = compositionLocalOf<RouteRegistry> {
    error("RouteRegistry not provided")
}

@Composable
fun AppNavigation(
    coordinator: AppCoordinator,
    routeProviders: List<RouteProvider> = listOf(RestaurantRouteProvider())
) {
    val navController = rememberNavController()
    val registry = remember { RouteRegistry() }
    val trackedRoutes = remember { mutableStateOf(Route.rootRoutes.map { it.key }.toSet()) }
    
    LaunchedEffect(coordinator) {
        coordinator.navigationEvents.collectLatest { event ->
            handleNavigationEvent(event, navController, trackedRoutes, registry, routeProviders)
        }
    }
    
    CompositionLocalProvider(
        LocalRouteRegistry provides registry
    ) {
        NavHost(
            navController = navController,
            startDestination = Destination.ROUTE_RESTAURANT_LIST
        ) {
            routeProviders.forEach { provider ->
                buildNavGraphForProvider(provider, this, coordinator)
            }
        }
    }
}

/**
 * Dynamically build navigation graph entries for all routes from a provider.
 */
private fun buildNavGraphForProvider(
    provider: RouteProvider,
    navGraphBuilder: NavGraphBuilder,
    coordinator: AppCoordinator
) {
    provider.getRoutes().forEach { handler ->
        buildRouteGraphEntry(handler, navGraphBuilder, coordinator)
    }
}

/**
 * Build a single route graph entry for the given handler.
 */
private fun buildRouteGraphEntry(
    handler: RouteHandler,
    navGraphBuilder: NavGraphBuilder,
    coordinator: AppCoordinator
) {
    val routeString = handler.toRouteString()
    when (routeString) {
        Destination.ROUTE_RESTAURANT_LIST -> {
            navGraphBuilder.composable(Destination.ROUTE_RESTAURANT_LIST) {
                RestaurantListScreen(coordinator)
            }
        }
        Destination.ROUTE_RESTAURANT_DETAIL -> {
            navGraphBuilder.composable(
                route = Destination.ROUTE_RESTAURANT_DETAIL,
                arguments = listOf(
                    navArgument(Destination.ARG_RESTAURANT_ID) { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val restaurantId = backStackEntry.arguments?.getString(Destination.ARG_RESTAURANT_ID) ?: ""
                RestaurantDetailScreen(restaurantId, coordinator)
            }
        }
    }
}

private fun handleNavigationEvent(
    event: NavigationEvent,
    navController: NavHostController,
    trackedRoutes: androidx.compose.runtime.MutableState<Set<String>>,
    registry: RouteRegistry,
    routeProviders: List<RouteProvider>
) {
    when (event) {
        is NavigationEvent.Push -> {
            var handled = false
            for (provider in routeProviders) {
                val handler = provider.getRoutes().firstOrNull { it.canHandle(event.destination) }
                if (handler != null) {
                    val route = handler.destinationToRoute(event.destination)
                    if (route != null) {
                        trackedRoutes.value += route.key
                        val navigationRoute = when (route) {
                            is RestaurantDetailRoute -> "${Destination.ROUTE_RESTAURANT_DETAIL_BASE}/${route.restaurantId}"
                            else -> handler.toRouteString()
                        }
                        navController.navigate(navigationRoute)
                        handled = true
                        break
                    }
                }
            }

            if (!handled) {
                throw IllegalArgumentException("No route provider found for destination: ${event.destination}")
            }
        }
        is NavigationEvent.Pop -> {
            val currentDestination = navController.currentDestination?.route

            navController.popBackStack()

            val updatedRoutes = trackedRoutes.value.toMutableSet()
            if (currentDestination?.startsWith(Destination.ROUTE_RESTAURANT_DETAIL_BASE) == true) {
                updatedRoutes.removeAll { it.startsWith(RestaurantDetailRoute.KEY_PREFIX) }
            }
            trackedRoutes.value = updatedRoutes

            registry.cleanup(trackedRoutes.value)
        }
        is NavigationEvent.PopToRoot -> {
            navController.popBackStack(
                route = Destination.ROUTE_RESTAURANT_LIST,
                inclusive = false
            )
            trackedRoutes.value = Route.rootRoutes.map { it.key }.toSet()
            registry.cleanup(trackedRoutes.value)
        }
    }
}
