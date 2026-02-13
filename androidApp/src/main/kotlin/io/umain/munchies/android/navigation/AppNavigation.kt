package io.umain.munchies.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.umain.munchies.android.features.restaurant.presentation.restaurantdetail.RestaurantDetailScreen
import io.umain.munchies.android.features.restaurant.presentation.restaurantlist.RestaurantListScreen
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.NavigationEvent
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.Route
import kotlinx.coroutines.flow.collectLatest

val LocalRouteRegistry = compositionLocalOf<RouteRegistry> {
    error("RouteRegistry not provided")
}

@Composable
fun AppNavigation(coordinator: AppCoordinator) {
    val navController = rememberNavController()
    val registry = remember { RouteRegistry() }
    val trackedRoutes = remember { mutableStateOf(Route.rootRoutes.map { it.key }.toSet()) }
    
    LaunchedEffect(coordinator) {
        coordinator.navigationEvents.collectLatest { event ->
            handleNavigationEvent(event, navController, trackedRoutes, registry)
        }
    }
    
    androidx.compose.runtime.CompositionLocalProvider(
        LocalRouteRegistry provides registry
    ) {
        NavHost(
            navController = navController,
            startDestination = Destination.ROUTE_RESTAURANT_LIST
        ) {
            composable(Destination.ROUTE_RESTAURANT_LIST) {
                RestaurantListScreen(coordinator)
            }
            
            composable(
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
    registry: RouteRegistry
) {
    when (event) {
        is NavigationEvent.Push -> {
            when (event.destination) {
                is Destination.RestaurantDetail -> {
                    val detail = event.destination as Destination.RestaurantDetail
                    trackedRoutes.value = trackedRoutes.value + RestaurantDetailRoute(detail.restaurantId).key
                }
                else -> {}
            }
            val route = event.destination.toRoute()
            navController.navigate(route)
        }
        is NavigationEvent.Pop -> {
            // Before popping, identify the current destination being removed
            val currentDestination = navController.currentDestination?.route

            navController.popBackStack()

            // Update tracked routes based on the destination that was removed
            val updatedRoutes = trackedRoutes.value.toMutableSet()
            when {
                currentDestination?.startsWith(Destination.ROUTE_RESTAURANT_DETAIL) == true -> {
                    // Remove the detail route that was just popped
                    // Since we're removing from the back stack, identify which detail route was removed
                    updatedRoutes.removeAll { it.startsWith("RestaurantDetail_") }
                }
            }
            trackedRoutes.value = updatedRoutes

            // Cleanup routes that are no longer in the stack
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
