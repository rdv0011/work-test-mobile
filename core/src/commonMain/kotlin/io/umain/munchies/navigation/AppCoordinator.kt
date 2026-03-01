package io.umain.munchies.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import io.umain.munchies.core.ui.TextId
import io.umain.munchies.core.ui.IconId

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
    initialState: NavigationState = createInitialTabNavigationState()
) {
    // === INTERNAL STATE ===

    private val _navigationState = MutableStateFlow(initialState)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    // === EVENTS (for platform layer) ===

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(
        replay = 1,
        extraBufferCapacity = 10
    )
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

     // === ROUTE HANDLERS (injected) ===

     var routeHandlers: List<RouteHandler> = emptyList()

      // === LISTENER READINESS ===

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

     // === PUBLIC API: SCREEN NAVIGATION ===

     /**
      * Navigate to a screen
      */
      open fun navigateToScreen(destination: Destination) {
          _navigationEvents.tryEmit(NavigationEvent.Push(destination))
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
           _navigationEvents.tryEmit(NavigationEvent.Pop)
       }

    /**
     * Return to root screen(s)
     */
    fun navigateToRoot() {
        _navigationEvents.tryEmit(NavigationEvent.PopToRoot)
    }

    // === PUBLIC API: MODAL NAVIGATION ===

    /**
     * Show a modal dialog
     */
    open fun showModal(destination: ModalDestination) {
        _navigationEvents.tryEmit(NavigationEvent.ShowModal(destination))
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
        _navigationEvents.tryEmit(NavigationEvent.DismissModal)
    }

    /**
     * Dismiss all modals
     */
    fun dismissAllModals() {
        _navigationEvents.tryEmit(NavigationEvent.DismissAllModals)
    }

    /**
     * Dismiss modals until condition is met
     */
    fun dismissModalUntil(predicate: (ModalRoute) -> Boolean) {
        _navigationEvents.tryEmit(NavigationEvent.DismissModalUntil(predicate))
    }

    // === PUBLIC API: TAB NAVIGATION ===

    /**
     * Switch to a tab
     */
    open fun selectTab(tabId: String) {
        _navigationEvents.tryEmit(NavigationEvent.SelectTab(tabId))
    }

    /**
     * Navigate within current tab
     */
    fun navigateInTab(destination: Destination) {
        _navigationEvents.tryEmit(NavigationEvent.PushInTab(destination))
    }

    /**
     * Go back in current tab
     */
    fun backInTab() {
        _navigationEvents.tryEmit(NavigationEvent.PopInTab)
    }

    // === PUBLIC API: STATE MANAGEMENT ===

    /**
     * Apply a complete navigation state (for deep links)
     */
    fun applyNavigationState(newState: NavigationState, clearCurrentStack: Boolean = true) {
        _navigationEvents.tryEmit(
            NavigationEvent.ApplyNavigationState(newState, clearCurrentStack)
        )
    }
    
    /**
     * Apply navigation state from a deep link URL
     */
    fun applyDeepLink(deepLink: String) {
        val navigationState = DeepLinkParser.parseDeepLink(deepLink)
        applyNavigationState(navigationState)
    }

    /**
     * Get current state (snapshot)
     */
    fun getCurrentState(): NavigationState {
        return _navigationState.value
    }

    /**
     * Reduce current state with event and update internal state
     * 
     * Called by platform layers to process navigation events and update state.
     */
    open fun reduceState(event: NavigationEvent) {
        val currentState = _navigationState.value
        val newState = NavigationReducer.reduce(currentState, event, routeHandlers)
        _navigationState.value = newState
    }

    companion object {
        /**
         * Create initial navigation state with tab navigation enabled
         */
        private fun createInitialTabNavigationState(): NavigationState {
            val restaurantsTab = TabDefinition(
                id = "restaurants",
                label = TextId.Restaurants,
                icon = IconId.Restaurant,
                rootRoute = RestaurantListRoute()
            )

            val settingsTab = TabDefinition(
                id = "settings",
                label = TextId.Settings,
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
                usesTabs = true
            )
        }
    }
}
