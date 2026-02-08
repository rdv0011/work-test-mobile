package io.umain.munchies.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.Route
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppNavigation(coordinator: AppCoordinator) {
    val navController = rememberNavController()
    val registry = remember { RouteRegistry() }
    val trackedRoutes = remember { mutableStateOf(Route.rootRoutes.map { it.key }.toSet()) }
    
    LaunchedEffect(coordinator) {
        coordinator.navigationEvents.collectLatest { event ->
            handleNavigationEvent(event, navController, trackedRoutes)
        }
    }

    LaunchedEffect(navController) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            registry.cleanup(trackedRoutes.value)
        }
    }
    
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

private fun handleNavigationEvent(
    event: NavigationEvent,
    navController: NavHostController,
    trackedRoutes: androidx.compose.runtime.MutableState<Set<String>>
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
            navController.popBackStack()
            trackedRoutes.value = trackedRoutes.value.toMutableSet().apply {
                remove(this.lastOrNull())
            }
        }
        is NavigationEvent.PopToRoot -> {
            navController.popBackStack(
                route = Destination.ROUTE_RESTAURANT_LIST,
                inclusive = false
            )
            trackedRoutes.value = Route.rootRoutes.map { it.key }.toSet()
        }
    }
}
