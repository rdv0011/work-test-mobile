package io.umain.munchies.navigation

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
    val currentStack: List<ScreenEntry>
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
