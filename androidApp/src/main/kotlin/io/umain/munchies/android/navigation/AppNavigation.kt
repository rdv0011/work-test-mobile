package io.umain.munchies.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.umain.munchies.android.features.restaurantdetail.RestaurantDetailScreen
import io.umain.munchies.android.features.restaurantlist.RestaurantListScreen
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.NavigationEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AppNavigation(coordinator: AppCoordinator) {
    val navController = rememberNavController()
    
    LaunchedEffect(coordinator) {
        coordinator.navigationEvents.collectLatest { event ->
            handleNavigationEvent(event, navController)
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
                navArgument("restaurantId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: ""
            RestaurantDetailScreen(restaurantId, coordinator)
        }
    }
}

private fun handleNavigationEvent(event: NavigationEvent, navController: NavHostController) {
    when (event) {
        is NavigationEvent.Push -> {
            val route = event.destination.toRoute()
            navController.navigate(route)
        }
        is NavigationEvent.Pop -> {
            navController.popBackStack()
        }
        is NavigationEvent.PopToRoot -> {
            navController.popBackStack(
                route = Destination.ROUTE_RESTAURANT_LIST,
                inclusive = false
            )
        }
    }
}
