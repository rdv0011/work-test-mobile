package io.umain.munchies.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

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
class AppCoordinator(
    initialState: NavigationState = NavigationState(
        primaryStack = listOf(RestaurantListRoute())
    )
) {
    // === INTERNAL STATE ===

    private val _navigationState = MutableStateFlow(initialState)
    val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    // === EVENTS (for platform layer) ===

    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(
        extraBufferCapacity = 10
    )
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    // === ROUTE HANDLERS (injected) ===

    var routeHandlers: List<RouteHandler> = emptyList()

    // === PUBLIC API: SCREEN NAVIGATION ===

    /**
     * Navigate to a screen
     */
    fun navigateToScreen(destination: Destination) {
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
    fun showModal(destination: ModalDestination) {
        _navigationEvents.tryEmit(NavigationEvent.ShowModal(destination))
    }

    /**
     * Show filter modal
     */
    fun showFilterModal(preSelectedFilters: List<String> = emptyList()) {
        showModal(ModalDestination.Filter(preSelectedFilters))
    }

    /**
     * Show confirmation dialog
     */
    fun showConfirmation(
        message: String,
        confirmText: String = "OK",
        cancelText: String = "Cancel"
    ) {
        showModal(ModalDestination.ConfirmAction(message, confirmText, cancelText))
    }

    /**
     * Show reviews modal for a restaurant
     */
    fun showReviews(restaurantId: String) {
        showModal(ModalDestination.Reviews(restaurantId))
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
    fun selectTab(tabId: String) {
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
     * Get current state (snapshot)
     */
    fun getCurrentState(): NavigationState {
        return _navigationState.value
    }

    /**
     * Reduce current state with event and update internal state
     */
    internal fun reduceState(event: NavigationEvent) {
        val currentState = _navigationState.value
        val newState = NavigationReducer.reduce(currentState, event, routeHandlers)
        _navigationState.value = newState
    }
}
