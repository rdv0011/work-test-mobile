package io.umain.munchies.android.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.umain.munchies.feature.restaurant.di.getRestaurantNavigationViewModel
import io.umain.munchies.feature.restaurant.di.getRestaurantDetailViewModel
import io.umain.munchies.feature.settings.di.getSettingsNavigationViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.umain.munchies.navigation.DeepLinkConstants
import io.umain.munchies.navigation.DeepLinkProcessor
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.Destination
import io.umain.munchies.navigation.NavigationEvent
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.SettingsRoute
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.RouteComposableBuilder
import io.umain.munchies.navigation.RouteNavigationMapper
import io.umain.munchies.navigation.RouteProvider
import io.umain.munchies.navigation.ScopedRouteHandler
import io.umain.munchies.navigation.ModalDestination
import io.umain.munchies.navigation.ModalRoute
import io.umain.munchies.navigation.FilterModalRoute
import io.umain.munchies.navigation.SubmitReviewModalRoute
import io.umain.munchies.navigation.ConfirmActionModalRoute
import io.umain.munchies.navigation.DatePickerModalRoute
import io.umain.munchies.navigation.ReviewSuccessModalRoute
import io.umain.munchies.navigation.ReviewErrorAlertRoute
import io.umain.munchies.android.features.settings.presentation.SettingsScreen
import io.umain.munchies.android.features.restaurant.presentation.restaurantlist.RestaurantListScreen
import io.umain.munchies.android.features.restaurant.presentation.restaurantdetail.RestaurantDetailScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

val LocalRouteRegistry = compositionLocalOf<RouteRegistry> {
    error("RouteRegistry not provided")
}

@Composable
fun AppNavigation(
    coordinator: AppCoordinator,
    pendingDeepLinkUri: Uri? = null,
    routeProviders: List<RouteProvider> = AndroidAppRouteProviders.create().getAllProviders()
) {
    val navController = rememberNavController()
    val allHandlers = remember {
        routeProviders.flatMap { it.getRoutes() }.filterIsInstance<ScopedRouteHandler>()
    }
    
    LaunchedEffect(allHandlers) {
        coordinator.routeHandlers = allHandlers
    }
    val scopedRouteHandlerRegistry = remember { ScopedRouteHandlerRegistry(allHandlers) }
    val registry = remember { RouteRegistry(scopedRouteHandlerRegistry) }
    
    val navigationMappers = remember {
        routeProviders.flatMap { it.getRoutes() }.filterIsInstance<RouteNavigationMapper>()
    }
    
    val trackedRouteKeys = remember { 
        mutableStateOf(Route.rootRoutes.map { it.key }.toSet())
    }
    
    val modalStack = remember { 
        mutableStateOf<List<ModalRoute>>(emptyList())
    }
    
    val composableBuilders = remember {
        routeProviders.flatMap { it.getRoutes() }.filterIsInstance<RouteComposableBuilder>()
    }
    
    val startDestination = remember {
        navigationMappers.firstNotNullOf { mapper ->
            mapper.mapDestinationToNavRoute(Destination.RestaurantList)
        }
    }
    
    val deepLinkProcessed = remember { mutableStateOf(pendingDeepLinkUri == null) }

    val navigationState = coordinator.navigationState.collectAsState().value

    LaunchedEffect(coordinator, pendingDeepLinkUri) {
        launch {
            try {
                coordinator.navigationEvents.collectLatest { event ->
                    coordinator.reduceState(event)
                    handleNavigationEvent(
                        event, 
                        navController, 
                        trackedRouteKeys,
                        modalStack,
                        registry, 
                        navigationMappers,
                        allHandlers,
                        startDestination,
                        navigationState
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Process deep link (will be replayed to subscriber due to replay=1 on SharedFlow)
        if (pendingDeepLinkUri != null) {
            processPendingDeepLink(pendingDeepLinkUri, coordinator)
        } else {
            coordinator.markListenerReady()
        }
        
        deepLinkProcessed.value = true
    }

    CompositionLocalProvider(
        LocalRouteRegistry provides registry
    ) {
        val usesTabs = navigationState.usesTabs
        val tabNavState = navigationState.tabNavigation

        // Only render navigation content after pending deep link has been processed
        if (deepLinkProcessed.value) {
            if (usesTabs && tabNavState != null) {
                    TabNavigationScaffold(
                        tabNavigationState = tabNavState,
                        coordinator = coordinator,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        renderTabContent(tabNavState)
                        renderModalsIfNeeded(modalStack, registry, coordinator)
                    }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composableBuilders.forEach { builder ->
                            builder.buildComposable(this, coordinator)
                        }
                    }
                    
                    renderModalsIfNeeded(modalStack, registry, coordinator)
                }
            }
        }
    }
}

@Composable
private fun renderTabContent(
    tabNavState: io.umain.munchies.navigation.TabNavigationState,
) {
    val activeStack = tabNavState.getActiveTabStack()
    if (activeStack.isNotEmpty()) {
        val topRoute = activeStack.last()
        renderCurrentScreen(topRoute)
    }
}

@Composable
private fun renderCurrentScreen(
    route: Route,
) {
    val registry = LocalRouteRegistry.current
    when (route) {
        is RestaurantListRoute -> {
            val scope = registry.createScopeForRoute(route)
            val navigationViewModel = scope.getRestaurantNavigationViewModel()
            RestaurantListScreen(navigationViewModel)
        }
        is RestaurantDetailRoute -> {
            val scope = registry.createScopeForRoute(route)
            val navigationViewModel = scope.getRestaurantNavigationViewModel()
            RestaurantDetailScreen(route.restaurantId, navigationViewModel)
        }
        is SettingsRoute -> {
            val scope = registry.createScopeForRoute(route)
            val navigationViewModel = scope.getSettingsNavigationViewModel()
            SettingsScreen(navigationViewModel)
        }
    }
}

@Composable
private fun renderModalsIfNeeded(
    modalStack: androidx.compose.runtime.MutableState<List<ModalRoute>>,
    registry: RouteRegistry,
    coordinator: AppCoordinator
) {
    if (modalStack.value.isNotEmpty()) {
        val currentModal = modalStack.value.last()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                .clickable(enabled = true) {
                    modalStack.value = modalStack.value.dropLast(1)
                },
            contentAlignment = Alignment.Center
        ) {
            ModalDestinationComposable(
                modal = currentModal,
                coordinator = coordinator,
                onDismiss = {
                    modalStack.value = modalStack.value.dropLast(1)
                },
                viewModelProvider = {
                    if (currentModal is SubmitReviewModalRoute) {
                        try {
                            val route = RestaurantDetailRoute(currentModal.restaurantId)
                            val scope = registry.createScopeForRoute(route)
                            scope.get<io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel>()
                        } catch (e: Exception) {
                            null
                        }
                    } else {
                        null
                    }
                }
            )
        }
    }
}

private fun handleNavigationEvent(
    event: NavigationEvent,
    navController: NavHostController,
    trackedRouteKeys: androidx.compose.runtime.MutableState<Set<String>>,
    modalStack: androidx.compose.runtime.MutableState<List<ModalRoute>>,
    registry: RouteRegistry,
    navigationMappers: List<RouteNavigationMapper>,
    allHandlers: List<ScopedRouteHandler>,
    rootDestinationRoute: String,
    navigationState: io.umain.munchies.navigation.NavigationState
) {
    // When using tabs, only handle modal and tab-specific events
    val usesTabs = navigationState.usesTabs
    
    when (event) {
        is NavigationEvent.Push -> {
            if (usesTabs) {
                // In tab mode, push events are handled by tab navigation
                // Just update modal stack if needed
            } else {
                handleNavigationPush(event, navController, trackedRouteKeys, navigationMappers, allHandlers)
            }
        }
        is NavigationEvent.Pop -> {
            if (usesTabs) {
                // In tab mode, pop events are handled by tab navigation
            } else {
                handleNavigationPop(navController, trackedRouteKeys, registry, navigationMappers)
            }
        }
        is NavigationEvent.PopToRoot -> {
            if (usesTabs) {
                // In tab mode, pop to root is handled by tab navigation
            } else {
                handleNavigationPopToRoot(navController, trackedRouteKeys, registry, rootDestinationRoute)
            }
        }
        is NavigationEvent.ShowModal -> {
            modalStack.value += event.destination.toModalRoute()
        }
        is NavigationEvent.DismissModal -> {
            if (modalStack.value.isNotEmpty()) {
                modalStack.value = modalStack.value.dropLast(1)
            }
        }
        is NavigationEvent.DismissAllModals -> {
            modalStack.value = emptyList()
        }
        is NavigationEvent.DismissModalUntil -> {
            val indexToKeep = modalStack.value.indexOfFirst { event.predicate(it) }
            if (indexToKeep >= 0) {
                modalStack.value = modalStack.value.take(indexToKeep)
            } else {
                modalStack.value = emptyList()
            }
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
            if (usesTabs) {
                // In tab mode, don't use NavController
                modalStack.value = event.newState.modalStack
            } else {
                handleApplyNavigationState(event, navController, trackedRouteKeys, registry, navigationMappers, rootDestinationRoute)
                modalStack.value = event.newState.modalStack
            }
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

private fun handleApplyNavigationState(
    event: NavigationEvent.ApplyNavigationState,
    navController: NavHostController,
    trackedRouteKeys: androidx.compose.runtime.MutableState<Set<String>>,
    registry: RouteRegistry,
    navigationMappers: List<RouteNavigationMapper>,
    rootDestinationRoute: String
) {
    val newState = event.newState
    
    if (event.clearCurrentStack) {
        navController.popBackStack(
            route = rootDestinationRoute,
            inclusive = false
        )
        trackedRouteKeys.value = Route.rootRoutes.map { it.key }.toSet()
    }
    
    for (route in newState.currentStack) {
        trackedRouteKeys.value += route.key
        val destination = when (route) {
            is RestaurantListRoute -> Destination.RestaurantList
            is RestaurantDetailRoute -> Destination.RestaurantDetail(route.restaurantId)
            else -> continue
        }
        
        val navRoute = navigationMappers.firstNotNullOfOrNull { mapper ->
            mapper.mapDestinationToNavRoute(destination)
        } ?: continue
        
        navController.navigate(navRoute)
    }
    
    registry.cleanup(trackedRouteKeys.value)
}

private fun ModalDestination.toModalRoute(): ModalRoute {
    return when (this) {
        is ModalDestination.Filter -> FilterModalRoute(preSelectedFilters)
        is ModalDestination.SubmitReviewModal -> SubmitReviewModalRoute(restaurantId)
        is ModalDestination.ConfirmAction -> ConfirmActionModalRoute(message, confirmText, cancelText)
        is ModalDestination.DatePicker -> DatePickerModalRoute(initialDate)
        is ModalDestination.ReviewErrorAlert -> ReviewErrorAlertRoute(message)
        ModalDestination.ReviewSuccessModal -> ReviewSuccessModalRoute
    }
}

private fun processPendingDeepLink(deepLinkUri: Uri, coordinator: AppCoordinator) {
    val host = deepLinkUri.host ?: return
    val pathSegments = deepLinkUri.pathSegments
    
    val queryParams = mutableMapOf<String, String>()
    listOf(
        DeepLinkConstants.QUERY_PARAM_FILTERS,
        DeepLinkConstants.QUERY_PARAM_MESSAGE,
        DeepLinkConstants.QUERY_PARAM_CONFIRM_TEXT,
        DeepLinkConstants.QUERY_PARAM_CANCEL_TEXT,
        DeepLinkConstants.QUERY_PARAM_INITIAL_DATE
    ).forEach { param ->
        val value = deepLinkUri.getQueryParameter(param)
        if (value != null) {
            queryParams[param] = value
        }
    }
    
    DeepLinkProcessor.processDeepLink(
        host = host,
        pathSegments = pathSegments,
        queryParams = queryParams,
        coordinator = coordinator
    )
}
