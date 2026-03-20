package io.umain.munchies.android.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import io.umain.munchies.android.features.restaurant.presentation.restaurantdetail.RestaurantDetailScreen
import io.umain.munchies.android.features.restaurant.presentation.restaurantlist.RestaurantListScreen
import io.umain.munchies.android.features.settings.presentation.SettingsScreen
import io.umain.munchies.feature.restaurant.di.getRestaurantNavigationViewModel
import io.umain.munchies.feature.settings.di.getSettingsNavigationViewModel
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.ConfirmActionModalRoute
import io.umain.munchies.navigation.DatePickerModalRoute
import io.umain.munchies.navigation.DeepLinkConstants
import io.umain.munchies.navigation.DeepLinkProcessor
import io.umain.munchies.navigation.FilterModalRoute
import io.umain.munchies.navigation.ModalDestination
import io.umain.munchies.navigation.ModalRoute
import io.umain.munchies.navigation.NavigationDirection
import io.umain.munchies.navigation.NavigationEvent
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.ReviewErrorAlertRoute
import io.umain.munchies.navigation.ReviewSuccessModalRoute
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.RouteProvider
import io.umain.munchies.navigation.ScopedRouteHandler
import io.umain.munchies.navigation.SettingsRoute
import io.umain.munchies.navigation.SubmitReviewModalRoute
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

val LocalRouteRegistry = compositionLocalOf<RouteRegistry> {
    error("RouteRegistry not provided")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(
    coordinator: AppCoordinator,
    pendingDeepLinkUri: Uri? = null,
    routeProviders: List<RouteProvider> = AndroidAppRouteProviders.create().getAllProviders()
) {
    val allHandlers = remember {
        routeProviders.flatMap { it.getRoutes() }.filterIsInstance<ScopedRouteHandler>()
    }
    val scopedRouteHandlerRegistry = remember { ScopedRouteHandlerRegistry(allHandlers) }
    val registry = remember { RouteRegistry(scopedRouteHandlerRegistry) }
    val modalStack = remember {
        mutableStateOf<List<ModalRoute>>(emptyList())
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
                        modalStack,
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
        val tabNavState = navigationState.tabNavigation
        if (deepLinkProcessed.value) {
            TabNavigationScaffold(
                tabNavigationState = tabNavState,
                coordinator = coordinator,
                modifier = Modifier.fillMaxSize()
            ) {
                renderTabContent(tabNavState)
                renderModalsIfNeeded(modalStack, registry, coordinator)
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun renderTabContent(
    tabNavState: io.umain.munchies.navigation.TabNavigationState,
) {
    val stack = tabNavState.getActiveTabStack()
    val currentRoute = stack.lastOrNull()
    val navigationDirection = tabNavState.navigationDirection
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    AnimatedContent(
        targetState = currentRoute,
        transitionSpec = {
            if (navigationDirection == NavigationDirection.Forward) {
                ScreenTransitionAnimations.enter(isRtl)
                    .togetherWith(ScreenTransitionAnimations.exit(isRtl))
            } else {
                ScreenTransitionAnimations.popEnter(isRtl)
                    .togetherWith(ScreenTransitionAnimations.popExit(isRtl))
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { route ->
        RouteRenderer(route)
    }
}

@Composable
private fun RouteRenderer(route: Route?) {
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
        null -> Unit
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
    modalStack: androidx.compose.runtime.MutableState<List<ModalRoute>>,
) {
    when (event) {
        is NavigationEvent.ShowModal -> {
            destinationToModalRoute(event.destination)?.let { modalRoute ->
                modalStack.value += modalRoute
            }
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
        else -> {
            // All navigation is handled by the tab navigation composable
        }
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
    // Refactored: Deep links should select tab and push into tab stack only
    DeepLinkProcessor.processDeepLink(
        host = host,
        pathSegments = pathSegments,
        queryParams = queryParams,
        coordinator = coordinator
    )
}

private fun destinationToModalRoute(destination: ModalDestination): ModalRoute? {
    return when (destination) {
        is ModalDestination.Filter -> FilterModalRoute(destination.preSelectedFilters)
        is ModalDestination.SubmitReviewModal -> SubmitReviewModalRoute(destination.restaurantId)
        is ModalDestination.ConfirmAction -> ConfirmActionModalRoute(
            destination.message,
            destination.confirmText,
            destination.cancelText
        )
        is ModalDestination.DatePicker -> DatePickerModalRoute(destination.initialDate)
        is ModalDestination.ReviewSuccessModal -> ReviewSuccessModalRoute
        is ModalDestination.ReviewErrorAlert -> ReviewErrorAlertRoute(destination.message)
    }
}
