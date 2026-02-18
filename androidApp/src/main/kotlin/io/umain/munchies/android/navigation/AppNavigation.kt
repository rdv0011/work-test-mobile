package io.umain.munchies.android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.NavigationEvent
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.RouteComposableBuilder
import io.umain.munchies.navigation.RouteNavigationMapper
import io.umain.munchies.navigation.RouteProvider
import io.umain.munchies.navigation.ScopedRouteHandler
import kotlinx.coroutines.flow.collectLatest

val LocalRouteRegistry = compositionLocalOf<RouteRegistry> {
    error("RouteRegistry not provided")
}

@Composable
fun AppNavigation(
    coordinator: AppCoordinator,
    routeProviders: List<RouteProvider> = AndroidAppRouteProviders.create().getAllProviders()
) {
    val navController = rememberNavController()
    val allHandlers = remember {
        routeProviders.flatMap { it.getRoutes() }.filterIsInstance<ScopedRouteHandler>()
    }
    val scopedRouteHandlerRegistry = remember { ScopedRouteHandlerRegistry(allHandlers) }
    val registry = remember { RouteRegistry(scopedRouteHandlerRegistry) }
    
    val navigationMappers = remember {
        routeProviders.flatMap { it.getRoutes() }.filterIsInstance<RouteNavigationMapper>()
    }
    
    val trackedRouteKeys = remember { 
        mutableStateOf(Route.rootRoutes.map { it.key }.toSet())
    }
    
    val composableBuilders = remember {
        routeProviders.flatMap { it.getRoutes() }.filterIsInstance<RouteComposableBuilder>()
    }
    
    val startDestination = remember {
        navigationMappers.firstNotNullOf { mapper ->
            mapper.mapDestinationToNavRoute(io.umain.munchies.navigation.Destination.RestaurantList)
        }
    }

    LaunchedEffect(coordinator) {
        coordinator.navigationEvents.collectLatest { event ->
            handleNavigationEvent(
                event, 
                navController, 
                trackedRouteKeys,
                registry, 
                navigationMappers,
                allHandlers,
                startDestination
            )
        }
    }

    CompositionLocalProvider(
        LocalRouteRegistry provides registry
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composableBuilders.forEach { builder ->
                builder.buildComposable(this, coordinator)
            }
        }
    }
}

private fun handleNavigationEvent(
    event: NavigationEvent,
    navController: NavHostController,
    trackedRouteKeys: androidx.compose.runtime.MutableState<Set<String>>,
    registry: RouteRegistry,
    navigationMappers: List<RouteNavigationMapper>,
    allHandlers: List<ScopedRouteHandler>,
    rootDestinationRoute: String
) {
    when (event) {
        is NavigationEvent.Push -> {
            handleNavigationPush(event, navController, trackedRouteKeys, navigationMappers, allHandlers)
        }
        is NavigationEvent.Pop -> {
            handleNavigationPop(navController, trackedRouteKeys, registry, navigationMappers)
        }
        is NavigationEvent.PopToRoot -> {
            handleNavigationPopToRoot(navController, trackedRouteKeys, registry, rootDestinationRoute)
        }
        is NavigationEvent.ShowModal -> {
            // Modal handling will be implemented in UI layer
        }
        is NavigationEvent.DismissModal -> {
            // Modal dismissal handled by modal composable
        }
        is NavigationEvent.DismissAllModals -> {
            // Modal dismissal handled by modal composable
        }
        is NavigationEvent.DismissModalUntil -> {
            // Modal dismissal handled by modal composable
        }
        is NavigationEvent.SelectTab -> {
            // Tab selection handled in tab navigation composable
        }
        is NavigationEvent.PushInTab -> {
            // Push within tab handled by tab navigation composable
        }
        is NavigationEvent.PopInTab -> {
            // Pop within tab handled by tab navigation composable
        }
        is NavigationEvent.ApplyNavigationState -> {
            // Deep link state application - reconstruct navigation stack
        }
    }
}

private fun handleNavigationPush(
    event: NavigationEvent.Push,
    navController: NavHostController,
    trackedRouteKeys: androidx.compose.runtime.MutableState<Set<String>>,
    navigationMappers: List<RouteNavigationMapper>,
    allHandlers: List<ScopedRouteHandler>
) {
    for (handler in allHandlers) {
        if (handler.canHandle(event.destination)) {
            val route = handler.destinationToRoute(event.destination)
            if (route != null) {
                trackedRouteKeys.value += route.key
                
                val navRoute = navigationMappers.firstNotNullOfOrNull { mapper ->
                    mapper.mapDestinationToNavRoute(event.destination)
                } ?: throw IllegalArgumentException("No navigation route mapper found for destination: ${event.destination}")
                
                navController.navigate(navRoute)
                return
            }
        }
    }
    throw IllegalArgumentException("No route handler found for destination: ${event.destination}")
}

private fun handleNavigationPop(
    navController: NavHostController,
    trackedRouteKeys: androidx.compose.runtime.MutableState<Set<String>>,
    registry: RouteRegistry,
    navigationMappers: List<RouteNavigationMapper>,
) {
    val currentDestination = navController.currentDestination?.route
    navController.popBackStack()

    val updatedRouteKeys = trackedRouteKeys.value.toMutableSet()
    
    if (currentDestination != null) {
        // Find the mapper for the current destination by checking cleanup pattern match
        for (mapper in navigationMappers) {
            val cleanupPattern = mapper.getRouteCleanupPattern()
            
            // Match against nav cleanup pattern (e.g., "restaurant_detail/")
            if (cleanupPattern != null && currentDestination.startsWith(cleanupPattern)) {
                val keyPattern = mapper.getRouteKeyPattern()
                
                // If mapper provides key pattern, remove routes matching it
                if (keyPattern != null) {
                    updatedRouteKeys.removeAll { it.startsWith(keyPattern) }
                }
                break
            }
        }
    }
    
    trackedRouteKeys.value = updatedRouteKeys
    registry.cleanup(updatedRouteKeys)
}

private fun handleNavigationPopToRoot(
    navController: NavHostController,
    trackedRouteKeys: androidx.compose.runtime.MutableState<Set<String>>,
    registry: RouteRegistry,
    rootDestinationRoute: String
) {
    navController.popBackStack(
        route = rootDestinationRoute,
        inclusive = false
    )
    trackedRouteKeys.value = Route.rootRoutes.map { it.key }.toSet()
    registry.cleanup(trackedRouteKeys.value)
}
