package io.umain.munchies.navigation

/**
 * All possible navigation events in the application.
 *
 * These represent user intents (navigate to screen, show modal, etc)
 * and are emitted by the NavigationCoordinator.
 *
 * The reducer processes these events to transform NavigationState.
 */
sealed class NavigationEvent {
    // SCREEN NAVIGATION

    /**
     * Navigate to a new screen (push to primary stack or active tab stack)
     */
    data class Push(val destination: Destination) : NavigationEvent()

    /**
     * Go back (pop from current stack or dismiss top modal)
     */
    data object Pop : NavigationEvent()

    /**
     * Return to root screen (clear stack to initial state)
     */
    data object PopToRoot : NavigationEvent()

    //  MODAL NAVIGATION

    /**
     * Show a modal overlay
     */
    data class ShowModal(val destination: ModalDestination) : NavigationEvent()

    /**
     * Dismiss top modal from modal stack
     */
    data object DismissModal : NavigationEvent()

    /**
     * Dismiss all modals from modal stack
     */
    data object DismissAllModals : NavigationEvent()

    /**
     * Dismiss modals until condition is met
     */
    data class DismissModalUntil(
        val predicate: (ModalRoute) -> Boolean
    ) : NavigationEvent()

    // TAB NAVIGATION

    /**
     * Switch to a different tab
     */
    data class SelectTab(val tabId: String) : NavigationEvent()

    /**
     * Push to the current tab's stack
     */
    data class PushInTab(val destination: Destination) : NavigationEvent()

    /**
     * Pop from the current tab's stack
     */
    data object PopInTab : NavigationEvent()

    // DEEP LINKING

    /**
     * Apply a complete navigation state (for deep links)
     */
    data class ApplyNavigationState(
        val newState: NavigationState,
        val clearCurrentStack: Boolean = true
    ) : NavigationEvent()
}

/**
 * Sealed class for modal destinations.
 * Similar to Destination but for modal overlays.
 */
sealed class ModalDestination {
    data class Filter(
        val preSelectedFilters: List<String> = emptyList()
    ) : ModalDestination()

    data class ConfirmAction(
        val message: String,
        val confirmText: String = "OK",
        val cancelText: String = "Cancel"
    ) : ModalDestination()

    data class DatePicker(
        val initialDate: String? = null
    ) : ModalDestination()

    data class SubmitReviewModal(
        val restaurantId: String
    ) : ModalDestination()
}
