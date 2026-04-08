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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import io.umain.munchies.android.features.restaurant.presentation.restaurantdetail.RestaurantDetailAndroidViewModel
import io.umain.munchies.android.features.restaurant.presentation.restaurantdetail.RestaurantDetailScreen
import io.umain.munchies.android.features.restaurant.presentation.restaurantlist.RestaurantListAndroidViewModel
import io.umain.munchies.android.features.restaurant.presentation.restaurantlist.RestaurantListScreen
import io.umain.munchies.android.features.settings.presentation.SettingsScreen
import io.umain.munchies.core.localization.StringResourceProvider
import io.umain.munchies.feature.restaurant.presentation.RestaurantDetailViewModel
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.ConfirmActionModalRoute
import io.umain.munchies.navigation.DatePickerModalRoute
import io.umain.munchies.navigation.DeepLinkConstants
import io.umain.munchies.navigation.DeepLinkProcessor
import io.umain.munchies.navigation.FilterModalRoute
import io.umain.munchies.navigation.ModalDestination
import io.umain.munchies.navigation.ModalRoute
import io.umain.munchies.navigation.NavigationDirection
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.navigation.RestaurantListRoute
import io.umain.munchies.navigation.ReviewErrorAlertRoute
import io.umain.munchies.navigation.ReviewSuccessModalRoute
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.SettingsRoute
import io.umain.munchies.navigation.SubmitReviewModalRoute
import io.umain.munchies.navigation.TabNavigationState
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.koin.java.KoinJavaComponent.getKoin

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(
    coordinator: AppCoordinator = koinInject(), // Use Koin for coordinator
    pendingDeepLinkUri: Uri? = null,
    stringProvider: StringResourceProvider = koinInject()
) {
    val navigationState = coordinator.navigationState.collectAsState().value
    val deepLinkProcessed = remember { mutableStateOf(pendingDeepLinkUri == null) }

    // Use only coordinator as the key for LaunchedEffect to avoid re-collecting on rotation
    LaunchedEffect(coordinator) {
        if (pendingDeepLinkUri != null) {
            processPendingDeepLink(pendingDeepLinkUri, coordinator)
        }
        coordinator.markListenerReady()
        deepLinkProcessed.value = true
    }

    val tabNavState = navigationState.tabNavigation
    if (deepLinkProcessed.value) {
        TabNavigationScaffold(
            tabNavigationState = tabNavState,
            coordinator = coordinator,
            stringProvider = stringProvider,
            modifier = Modifier.fillMaxSize()
        ) {
            RenderTabContent(tabNavState, stringProvider, coordinator)
            RenderModalsIfNeeded(navigationState.modalStack, coordinator)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun RenderTabContent(
    tabNavState: TabNavigationState,
    stringProvider: StringResourceProvider,
    coordinator: AppCoordinator
) {
    val stack = tabNavState.getActiveTabStack()
    val currentEntry = stack.lastOrNull()
    val navigationDirection = tabNavState.navigationDirection
    val layoutDirection = LocalLayoutDirection.current
    val isRtl = layoutDirection == LayoutDirection.Rtl

    AnimatedContent(
        targetState = currentEntry,
        transitionSpec = {
            when (navigationDirection) {
                NavigationDirection.Forward -> {
                    ScreenTransitionAnimations.enter(isRtl)
                        .togetherWith(ScreenTransitionAnimations.exit(isRtl))
                }
                NavigationDirection.Back -> {
                    ScreenTransitionAnimations.popEnter(isRtl)
                        .togetherWith(ScreenTransitionAnimations.popExit(isRtl))
                }
                NavigationDirection.TabSwitch -> {
                    ScreenTransitionAnimations.fadeOnlyTransition
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { route ->
        RouteRenderer(route, stringProvider, coordinator)
    }
}

@Composable
private fun RouteRenderer(route: Route?, stringProvider: StringResourceProvider, coordinator: AppCoordinator) {
    val koin = getKoin()
    when (route) {
        is RestaurantListRoute -> {
            val scopeId = remember(route.key) { "RestaurantListScope_${route.key.substringAfterLast("/")}" }
            val viewModel: RestaurantListAndroidViewModel = remember(scopeId) {
                val scope = koin.getScopeOrNull(scopeId)
                    ?: koin.createScope(
                        scopeId = scopeId,
                        qualifier = org.koin.core.qualifier.named(route.scopeQualifier)
                    )
                scope.get()
            }

            DisposableEffect(scopeId) {
                onDispose {
                    koin.getScopeOrNull(scopeId)?.close()
                }
            }

            RestaurantListScreen(
                viewModel = viewModel,
                navigationViewModel = koinInject(),
                stringProvider = stringProvider
            )
        }
        is RestaurantDetailRoute -> {
            val scopeId = remember(route.key) { "RestaurantDetailScope_${route.key.substringAfterLast("/")}" }
            val androidViewModel: RestaurantDetailAndroidViewModel = remember(scopeId) {
                val scope = koin.getScopeOrNull(scopeId)
                    ?: koin.createScope(
                        scopeId = scopeId,
                        qualifier = org.koin.core.qualifier.named(route.scopeQualifier)
                    )
                val sharedViewModel: RestaurantDetailViewModel =
                    scope.get(parameters = { parametersOf(route.restaurantId) })
                scope.get(parameters = { parametersOf(sharedViewModel) })
            }

            DisposableEffect(scopeId) {
                onDispose {
                    koin.getScopeOrNull(scopeId)?.close()
                }
            }

            RestaurantDetailScreen(
                viewModel = androidViewModel,
                stringProvider = stringProvider,
                onBackClick = { coordinator.navigateBack() },
            )
        }
        is SettingsRoute -> {
            val settingsViewModel = koinInject<io.umain.munchies.feature.settings.presentation.SettingsViewModel>()
            SettingsScreen(
                viewModel = settingsViewModel,
                stringProvider = stringProvider
            )
        }
        null -> Unit
    }
}

@Composable
private fun RenderModalsIfNeeded(
    modalStack: List<ModalRoute>,
    coordinator: AppCoordinator
) {
    if (modalStack.isNotEmpty()) {
        val currentModal = modalStack.last()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f))
                .clickable(enabled = true) {
                    coordinator.dismissModal()
                },
            contentAlignment = Alignment.Center
        ) {
            ModalDestinationComposable(
                modal = currentModal,
                coordinator = coordinator,
                onDismiss = {
                    coordinator.dismissModal()
                },
                viewModelProvider = {
                    (currentModal as? SubmitReviewModalRoute)?.let {
                        koinInject<RestaurantDetailViewModel>(parameters = { parametersOf(it.restaurantId) })
                    }
                }
            )
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
        deepLinkUri.getQueryParameter(param)?.let { value ->
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
