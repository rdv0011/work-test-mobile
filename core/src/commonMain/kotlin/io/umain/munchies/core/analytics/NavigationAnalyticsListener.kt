package io.umain.munchies.core.analytics

import io.umain.munchies.navigation.NavigationState
import io.umain.munchies.navigation.Route
import io.umain.munchies.navigation.RestaurantDetailRoute
import io.umain.munchies.core.lifecycle.KmpViewModel
import io.umain.munchies.core.util.currentTimeMillis
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class NavigationAnalyticsListener(
    private val analyticsService: AnalyticsService,
    private val navigationStateFlow: StateFlow<NavigationState>,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : KmpViewModel() {

    private var previousState: NavigationState? = null
    private val modalOpenTimes = mutableMapOf<String, Long>()

    fun startTracking() {
        scope.launch(dispatcher) {
            navigationStateFlow.collect { newState ->
                trackStateChanges(previousState, newState)
                previousState = newState
            }
        }
    }

    private suspend fun trackStateChanges(
        previousState: NavigationState?,
        currentState: NavigationState
    ) {
        if (previousState == null) {
            trackInitialScreen(currentState)
            return
        }

        trackScreenChanges(previousState, currentState)
        trackTabChanges(previousState, currentState)
        trackModalChanges(previousState, currentState)
    }

    private suspend fun trackInitialScreen(state: NavigationState) {
        val initialRoute = getCurrentRoute(state)
        initialRoute?.let {
            analyticsService.trackEvent(
                AnalyticsEvent.ScreenView(
                    screenName = it.key,
                    screenClass = it.key,
                    properties = extractRouteProperties(it)
                )
            )
        }
    }

    private suspend fun trackScreenChanges(
        previousState: NavigationState,
        currentState: NavigationState
    ) {
        val previousRoute = getCurrentRoute(previousState)
        val currentRoute = getCurrentRoute(currentState)

        if (previousRoute?.key != currentRoute?.key) {
            currentRoute?.let {
                analyticsService.trackEvent(
                    AnalyticsEvent.ScreenView(
                        screenName = it.key,
                        screenClass = it.key,
                        previousScreen = previousRoute?.key,
                        properties = extractRouteProperties(it)
                    )
                )
            }
        }
    }

    private suspend fun trackTabChanges(
        previousState: NavigationState,
        currentState: NavigationState
    ) {
        val previousTabId = previousState.tabNavigation?.activeTabId
        val currentTabId = currentState.tabNavigation?.activeTabId

        if (previousTabId != currentTabId && currentTabId != null) {
            analyticsService.trackEvent(
                AnalyticsEvent.TabSwitch(
                    tabId = currentTabId,
                    tabName = getTabLabel(currentState, currentTabId)
                )
            )
        }
    }

    private suspend fun trackModalChanges(
        previousState: NavigationState,
        currentState: NavigationState
    ) {
        val previousModals = previousState.modalStack
        val currentModals = currentState.modalStack

        if (currentModals.size > previousModals.size) {
            currentModals.lastOrNull()?.let { newModal ->
                modalOpenTimes[newModal.key] = currentTimeMillis()
                analyticsService.trackEvent(
                    AnalyticsEvent.ModalOpen(
                        modalName = newModal.key,
                        modalClass = newModal.key,
                        properties = extractRouteProperties(newModal)
                    )
                )
            }
        }

        if (currentModals.size < previousModals.size) {
            val dismissedModal = previousModals.getOrNull(currentModals.size)
            dismissedModal?.let { modal ->
                val timeSpent = currentTimeMillis() -
                    (modalOpenTimes.remove(modal.key) ?: currentTimeMillis())
                analyticsService.trackEvent(
                    AnalyticsEvent.ModalDismiss(
                        modalName = modal.key,
                        timeSpentMs = timeSpent
                    )
                )
            }
        }
    }

    private fun getCurrentRoute(state: NavigationState): Route? {
        return if (state.modalStack.isNotEmpty()) {
            state.modalStack.last()
        } else {
            state.tabNavigation?.stacksByTab
                ?.get(state.tabNavigation.activeTabId)
                ?.lastOrNull()
        }
    }

    private fun getTabLabel(state: NavigationState, tabId: String): String {
        return state.tabNavigation?.tabDefinitions
            ?.find { it.id == tabId }
            ?.label
            ?.toString() ?: tabId
    }

    private fun extractRouteProperties(route: Route): Map<String, String> {
        return when (route) {
            is RestaurantDetailRoute -> mapOf(
                "restaurant_id" to route.restaurantId
            )
            else -> emptyMap()
        }.let { sanitizeProperties(it) }
    }

    private fun sanitizeProperties(props: Map<String, String>): Map<String, String> {
        val sensitive = setOf(
            "email", "phone", "ssn", "password",
            "auth_token", "user_name", "api_key"
        )
        return props.filterKeys { it !in sensitive }
    }
}
