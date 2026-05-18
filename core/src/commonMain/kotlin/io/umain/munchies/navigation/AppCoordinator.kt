package io.umain.munchies.navigation

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import io.umain.munchies.core.localization.StringResources
import io.umain.munchies.core.ui.IconId
import io.umain.munchies.logging.logError
import io.umain.munchies.logging.logInfo
import io.umain.munchies.navigation.persistence.NavigationPersistenceStore
import io.umain.munchies.core.lifecycle.LifecycleOwner

/**
 * Central coordinator for all navigation in the application.
 *
 * Handles:
 * - Screen navigation (push/pop)
 * - Modal presentation/dismissal
 * - Tab switching
 * - Deep link processing
 *
 * Uses Redux pattern: events are dispatched, reduced to new state, and emitted.
 */
open class AppCoordinator(
    initialState: NavigationState = createInitialTabNavigationState(),
    private val routeHandlers: List<RouteHandler> = emptyList(),
    private val persistenceStore: NavigationPersistenceStore? = null
) : LifecycleOwner() {
    // INTERNAL STATE

    private val _navigationState = MutableStateFlow(initialState)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    private val persistenceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        // Create Koin scopes for all routes already present in the initial navigation state
        // (e.g. RestaurantListRoute and SettingsRoute that are pre-seeded as tab roots).
        initialState.getAllRoutes().forEach { route ->
            val destination = route.toDestination() ?: return@forEach
            val handler = routeHandlers
                .filterIsInstance<ScopedRouteHandler>()
                .firstOrNull { it.canHandle(destination) }
            if (handler != null) {
                logInfo("AppCoordinator", "🔧 init: Creating scope for initial route=${route.key}")
                handler.createScope(route)
            }
        }
    }

    // EVENTS (for platform layer)

    // replay = 1: Crucial for native platforms (like iOS) processing deep links on a cold start.
    // It captures initial pre-UI navigation events and ensures they aren't lost if the
    // listener binds a few milliseconds after the event was dispatched.
    // extraBufferCapacity: Enables rapid consecutive state reductions without blocking or dropping events.
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(
        replay = 1,
        extraBufferCapacity = 10
    )
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

      /**
       * ANALYTICS ARCHITECTURE NOTE (Phase 3):
       *
       * Implementation: Observer Pattern (Pull-based)
       * - NavigationAnalyticsListener observes this.navigationState StateFlow
       * - Listener independently tracks changes without AppCoordinator involvement
       * - Decoupled: AppCoordinator is unaware of analytics, no tight coupling
       * - Thread-safe: State changes are processed by NavigationAnalyticsListener's internal coroutine
       *
       * Why Observer Pattern was chosen:
       * 1. NavigationAnalyticsListener can be created/destroyed independently of coordinator
       * 2. No need to pass through AppCoordinator initialization
       * 3. Simpler lifecycle: listener cleanup in Activity.onDestroy()
       * 4. Less state mutation in coordinator (single responsibility)
       * 5. No need for listener registration/deregistration methods
       */

      // LISTENER READINESS

      private var isListenerReady = false
      private val pendingListenerCallbacks = mutableListOf<() -> Unit>()

       /**
        * Register a callback to execute when the navigation event listener is ready.
        * 
        * If called after listener is already ready, the callback executes immediately.
        * This eliminates the need for hardcoded timing delays on platform layers.
        * 
        * @param action Lambda to execute when listener is ready
        */
       fun onListenerReady(action: () -> Unit) {
           if (isListenerReady) {
               action()
           } else {
               pendingListenerCallbacks.add(action)
           }
       }

       fun markListenerReady() {
           if (!isListenerReady) {
               isListenerReady = true
               val callbacks = pendingListenerCallbacks.toList()
               pendingListenerCallbacks.clear()
               callbacks.forEach { it() }
           }
       }

     // PUBLIC API: SCREEN NAVIGATION

    open fun dispatch(event: NavigationEvent) {
        reduceState(event)
        _navigationEvents.tryEmit(event)
    }

     /**
      * Navigate to a screen
      */
      open fun navigateToScreen(destination: Destination) {
          dispatch(NavigationEvent.Push(destination))
      }

    /**
     * Convenience method for restaurant detail
     */
    fun navigateToRestaurantDetail(restaurantId: String) {
        navigateToScreen(Destination.RestaurantDetail(restaurantId))
    }

      /**
       * Go back (pops modal if showing, else pops screen)
       */
       fun navigateBack() {
           dispatch(NavigationEvent.Pop)
       }

    /**
     * Return to root screen(s)
     */
    fun navigateToRoot() {
        dispatch(NavigationEvent.PopToRoot)
    }

    // PUBLIC API: MODAL NAVIGATION

    /**
     * Show a modal dialog
     */
    open fun showModal(destination: ModalDestination) {
        dispatch(NavigationEvent.ShowModal(destination))
    }

    /**
     * Show filter modal
     */
    open fun showFilterModal(preSelectedFilters: List<String> = emptyList()) {
        showModal(ModalDestination.Filter(preSelectedFilters))
    }

    /**
     * Show confirmation dialog
     */
    open fun showConfirmation(
        message: String,
        confirmText: String = "OK",
        cancelText: String = "Cancel"
    ) {
        showModal(ModalDestination.ConfirmAction(message, confirmText, cancelText))
    }

    /**
     * Show submit review modal for a restaurant
     */
    open fun submitReview(restaurantId: String) {
        showModal(ModalDestination.SubmitReviewModal(restaurantId))
    }

    /**
     * Dismiss top modal
     */
    fun dismissModal() {
        dispatch(NavigationEvent.DismissModal)
    }

    /**
     * Dismiss all modals
     */
    fun dismissAllModals() {
        dispatch(NavigationEvent.DismissAllModals)
    }

    /**
     * Dismiss modals until condition is met
     */
    fun dismissModalUntil(predicate: (ModalRoute) -> Boolean) {
        dispatch(NavigationEvent.DismissModalUntil(predicate))
    }

    // PUBLIC API: TAB NAVIGATION

    /**
     * Switch to a tab
     */
    open fun selectTab(tabId: String) {
        dispatch(NavigationEvent.SelectTab(tabId))
    }

    /**
     * Navigate within current tab
     */
    fun navigateInTab(destination: Destination) {
        dispatch(NavigationEvent.PushInTab(destination))
    }

    /**
     * Go back in current tab
     */
    fun backInTab() {
        dispatch(NavigationEvent.PopInTab)
    }

    // PUBLIC API: STATE MANAGEMENT

    /**
     * Apply a complete navigation state (for deep links)
     */
    fun applyNavigationState(newState: NavigationState, clearCurrentStack: Boolean = true) {
        dispatch(NavigationEvent.ApplyNavigationState(newState, clearCurrentStack))
    }
    
    /**
     * Apply navigation state from a deep link URL
     */
    fun applyDeepLink(deepLink: String) {
        val parser = DeepLinkParser(routeHandlers.filterIsInstance<DeepLinkHandler>())
        when (val result = parser.parse(deepLink)) {
            is DeepLinkResult.Success -> applyNavigationState(result.navigationState, result.clearCurrentStack)
            is DeepLinkResult.Partial -> applyNavigationState(result.navigationState, result.clearCurrentStack)
            is DeepLinkResult.NotFound -> {
                // Optionally log or handle not found
                logInfo("AppCoordinator", "Deep link not found: $deepLink")
            }
            is DeepLinkResult.Error -> {
                // Optionally log or handle error
                logInfo("AppCoordinator", "Deep link error: ${result.link}, exception: ${result.exception}")
            }
        }
    }

    /**
     * Get current state (snapshot)
     */
    fun getCurrentState(): NavigationState {
        return _navigationState.value
    }

    /**
     * Reduce current state with event and update internal state.
     *
     * Scope lifecycle is fully owned here (per refactoring plan):
     * - Scopes for newly appearing routes are created via ScopedRouteHandler.createScope()
     * - Scopes for removed routes are closed via NavigationEffects
     * UI layer only resolves already-open scopes; it never creates or closes them.
     */
    open fun reduceState(event: NavigationEvent) {
        val currentState = _navigationState.value
        logInfo("AppCoordinator", "🔄 reduceState: Event=${event::class.simpleName}, handlers=${routeHandlers.size}")
        val newState = NavigationReducer.reduce(currentState, event, routeHandlers)

        // Create Koin scopes for routes that just entered the navigation state
        val addedRoutes = newState.getAllRoutes() - currentState.getAllRoutes()
        addedRoutes.forEach { route ->
            val destination = route.toDestination() ?: return@forEach
            val handler = routeHandlers
                .filterIsInstance<ScopedRouteHandler>()
                .firstOrNull { it.canHandle(destination) }
            if (handler != null) {
                logInfo("AppCoordinator", "🔧 Creating scope for route=${route.key}")
                handler.createScope(route)
            }
        }

        // Close Koin scopes for routes that just left the navigation state
        NavigationEffects.handleNavigationSideEffects(currentState, newState)

        logInfo("AppCoordinator", "🔄 reduceState done: Old=${getRouteKey(currentState)}, New=${getRouteKey(newState)}")
        _navigationState.value = newState
        logInfo("AppCoordinator", "✅ State updated and emitted via StateFlow")
        if (persistenceStore != null) {
            persistNavigationStateAsync(newState)
        }
    }

    private fun persistNavigationStateAsync(state: NavigationState) {
        persistenceScope.launch {
            try {
                val snapshot = state.toSnapshot()
                persistenceStore?.saveNavigationState(snapshot)
                    ?.onFailure { e -> logError("AppCoordinator", "Failed to persist navigation state: ${e.message}") }
            } catch (e: Exception) {
                logError("AppCoordinator", "Unexpected error persisting navigation state: ${e.message}")
            }
        }
    }
    
    private fun getRouteKey(state: NavigationState): String {
        return if (state.modalStack.isNotEmpty()) {
            "modal:${state.modalStack.last().key}"
        } else {
            state.tabNavigation.stacksByTab
                .get(state.tabNavigation.activeTabId)
                ?.lastOrNull()?.key ?: "unknown"
        }
    }

    companion object {
        /**
         * Create initial navigation state with tab navigation enabled
         */
        private fun createInitialTabNavigationState(): NavigationState {
            val restaurantsTab = TabDefinition(
                id = "restaurants",
                label = StringResources.tab_restaurants,
                icon = IconId.Restaurant,
                rootRoute = RestaurantListRoute()
            )

            val settingsTab = TabDefinition(
                id = "settings",
                label = StringResources.tab_settings,
                icon = IconId.Settings,
                rootRoute = SettingsRoute()
            )

            val tabNav = TabNavigationState(
                tabDefinitions = listOf(restaurantsTab, settingsTab),
                activeTabId = "restaurants",
                stacksByTab = mapOf(
                    "restaurants" to listOf(RestaurantListRoute()),
                    "settings" to listOf(SettingsRoute())
                )
            )

            return NavigationState(
                tabNavigation = tabNav,
            )
        }
    }
}
