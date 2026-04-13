package io.umain.munchies.navigation

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Complete navigation state of the application.
 *
 * Supports only tabbed navigation with per-tab stacks (tabNavigation).
 */
data class NavigationState(
    // Modal overlays (independent of tab stacks)
    val modalStack: List<ModalRoute> = emptyList(),

    // Tab navigation state (required)
    val tabNavigation: TabNavigationState,

    // Optional: Track the deep link that triggered this state
    val originDeepLink: String? = null
) {
    val navigationDirection: NavigationDirection
        get() = tabNavigation.navigationDirection

    /**
     * Get the current active stack (active tab's stack)
     */
    val currentStack: List<Route>
        get() = tabNavigation.getActiveTabStack()

    /**
     * Whether any modals are currently displayed
     */
    val hasModals: Boolean get() = modalStack.isNotEmpty()

    /**
     * Get the topmost modal, if any
     */
    val topModal: ModalRoute? get() = modalStack.lastOrNull()

    /**
     * Create a new state with updated modal stack
     */
    fun withModalStack(newModalStack: List<ModalRoute>): NavigationState {
        return copy(modalStack = newModalStack)
    }
}

abstract class StackRoute : Route() {
    override val isRootRoute: Boolean = false
}

@Serializable
sealed class ModalRoute : Route() {
    @Transient override val isRootRoute: Boolean = false

    abstract val presentationStyle: ModalPresentationStyle

    @Transient open val dismissOnBackgroundTap: Boolean = true
}

/**
 * Platform-specific modal presentation styles
 */
enum class ModalPresentationStyle {
    // Bottom sheet (Android: ModalBottomSheet, iOS: .sheet)
    SHEET,

    // Full-screen modal (Android: Dialog(fullscreen), iOS: .fullScreenCover)
    FULL_SCREEN,

    // Dialog (Android: AlertDialog, iOS: .alert)
    DIALOG
}
