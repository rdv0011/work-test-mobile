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
import io.umain.munchies.android.core.viewmodel.rememberScopedViewModel
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
import io.umain.munchies.navigation.ScopedRouteHandler
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
    val allHandlers = remember {
        routeProviders.flatMap { it.getRoutes() }.filterIsInstance<ScopedRouteHandler>()
    }
    val scopedRouteHandlerRegistry = remember { ScopedRouteHandlerRegistry(allHandlers) }
    val registry = remember { RouteRegistry(scopedRouteHandlerRegistry) }
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
            startDestination = "restaurant_list"
        ) {
            routeProviders.forEach { provider ->
                buildNavGraphForProvider(provider, this, coordinator)
            }
        }
    }
}

private fun buildNavGraphForProvider(
    provider: RouteProvider,
    navGraphBuilder: NavGraphBuilder,
    coordinator: AppCoordinator
) {
    provider.getRoutes().forEach { handler ->
        buildRouteGraphEntry(handler, navGraphBuilder, coordinator)
    }
}

private fun buildRouteGraphEntry(
    handler: RouteHandler,
    navGraphBuilder: NavGraphBuilder,
    coordinator: AppCoordinator
) {
    val routeString = handler.toRouteString()

    if (handler is ScopedRouteHandler) {
        when (handler.route) {
            is RestaurantDetailRoute -> {
                navGraphBuilder.composable(
                    route = routeString,
                    arguments = listOf(
                        navArgument("restaurantId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: ""
                    RestaurantDetailScreen(restaurantId, coordinator)
                }
            }
            else -> {
                navGraphBuilder.composable(routeString) {
                    when (handler.route) {
                        is io.umain.munchies.navigation.RestaurantListRoute -> {
                            RestaurantListScreen(coordinator)
                        }
                    }
                }
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
                            is RestaurantDetailRoute -> "${handler.toRouteString().substringBefore("/")}/${route.restaurantId}"
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
            if (currentDestination?.startsWith("restaurant_detail") == true) {
                updatedRoutes.removeAll { it.startsWith(RestaurantDetailRoute.KEY_PREFIX) }
            }
            trackedRoutes.value = updatedRoutes

            registry.cleanup(trackedRoutes.value)
        }
        is NavigationEvent.PopToRoot -> {
            navController.popBackStack(
                route = "restaurant_list",
                inclusive = false
            )
            trackedRoutes.value = Route.rootRoutes.map { it.key }.toSet()
            registry.cleanup(trackedRoutes.value)
        }
    }
}

