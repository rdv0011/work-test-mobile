package io.umain.munchies.navigation

/**
 * Complete navigation state of the application.
 *
 * Supports three navigation patterns:
 * 1. Linear stack navigation (primaryStack)
 * 2. Modal overlays (modalStack)
 * 3. Tabbed navigation with per-tab stacks (tabNavigation)
 */
data class NavigationState(
    // Primary navigation stack (used in non-tabbed apps)
    val primaryStack: List<Route> = emptyList(),

    // Modal overlays (independent of primary stack)
    val modalStack: List<ModalRoute> = emptyList(),

    // Tab navigation state (if app uses tabs)
    val tabNavigation: TabNavigationState? = null,

    // Flag indicating if this app uses tabs
    val usesTabs: Boolean = false,

    // Optional: Track the deep link that triggered this state
    val originDeepLink: String? = null
) {
    /**
     * Get the current active stack (either primaryStack or active tab's stack)
     */
    val currentStack: List<Route>
        get() = if (usesTabs) {
            tabNavigation?.getActiveTabStack() ?: emptyList()
        } else {
            primaryStack
        }

    /**
     * Whether any modals are currently displayed
     */
    val hasModals: Boolean get() = modalStack.isNotEmpty()

    /**
     * Get the topmost modal, if any
     */
    val topModal: ModalRoute? get() = modalStack.lastOrNull()

    /**
     * Create a new state with updated primary stack
     */
    fun withPrimaryStack(newStack: List<Route>): NavigationState {
        return copy(primaryStack = newStack)
    }

    /**
     * Create a new state with updated tab navigation
     */
    fun withTabNavigation(newTabNav: TabNavigationState?): NavigationState {
        return copy(tabNavigation = newTabNav)
    }

    /**
     * Create a new state with updated modal stack
     */
    fun withModalStack(newModalStack: List<ModalRoute>): NavigationState {
        return copy(modalStack = newModalStack)
    }
}

/**
 * Marks a Route as a stack-based screen route.
 * Screens can be pushed/popped from the back stack.
 * This is the default for most Route implementations.
 */
interface StackRoute : Route {
    override val isRootRoute: Boolean
        get() = false
}

/**
 * Marks a Route as a modal overlay route.
 * Modals are presented independently of the primary stack.
 */
interface ModalRoute : Route {
    override val isRootRoute: Boolean
        get() = false

    /**
     * Presentation style for this modal
     */
    val presentationStyle: ModalPresentationStyle
        get() = ModalPresentationStyle.SHEET

    /**
     * Whether tapping outside the modal dismisses it
     */
    val dismissOnBackgroundTap: Boolean
        get() = true
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
