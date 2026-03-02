package io.umain.munchies.core.analytics

import io.umain.munchies.core.util.currentTimeMillis

/**
 * Type-safe analytics event wrapper.
 * All events must be sealed for compile-time safety and exhaustive when expressions.
 *
 * Each event carries:
 * - eventName: The name of the event for analytics dashboards
 * - properties: Additional metadata (screen_name, restaurant_id, etc.)
 * - timestamp: When the event occurred
 */
sealed class AnalyticsEvent {
    abstract val eventName: String
    abstract val properties: Map<String, String>
    abstract val timestamp: Long

    /**
     * Track when user views a screen.
     * Automatically emitted on navigation via NavigationAnalyticsListener.
     */
    data class ScreenView(
        val screenName: String,
        val screenClass: String,
        val previousScreen: String? = null,
        override val properties: Map<String, String> = emptyMap(),
        override val timestamp: Long = currentTimeMillis()
    ) : AnalyticsEvent() {
        override val eventName: String = "screen_view"
    }

    /**
     * Track when user switches tabs.
     * Automatically emitted on tab switch via NavigationAnalyticsListener.
     */
    data class TabSwitch(
        val tabId: String,
        val tabName: String,
        override val properties: Map<String, String> = emptyMap(),
        override val timestamp: Long = currentTimeMillis()
    ) : AnalyticsEvent() {
        override val eventName: String = "tab_switch"
    }

    /**
     * Track when a modal is presented.
     * Automatically emitted on modal presentation via NavigationAnalyticsListener.
     */
    data class ModalOpen(
        val modalName: String,
        val modalClass: String,
        override val properties: Map<String, String> = emptyMap(),
        override val timestamp: Long = currentTimeMillis()
    ) : AnalyticsEvent() {
        override val eventName: String = "modal_open"
    }

    /**
     * Track when a modal is dismissed.
     * Automatically emitted on modal dismissal via NavigationAnalyticsListener.
     *
     * @param timeSpentMs Time spent in the modal before dismissal
     */
    data class ModalDismiss(
        val modalName: String,
        val timeSpentMs: Long,
        override val properties: Map<String, String> = emptyMap(),
        override val timestamp: Long = currentTimeMillis()
    ) : AnalyticsEvent() {
        override val eventName: String = "modal_dismiss"
    }

    /**
     * Custom event for business logic tracking.
     * Use this for optional, explicit event tracking in ViewModels
     * (e.g., filter_applied, review_submitted).
     */
    data class CustomEvent(
        override val eventName: String,
        override val properties: Map<String, String> = emptyMap(),
        override val timestamp: Long = currentTimeMillis()
    ) : AnalyticsEvent()

    /**
     * Track application errors.
     * Optional: implement in ViewModels when catching exceptions.
     */
    data class ErrorEvent(
        val errorType: String,
        val errorMessage: String,
        val errorContext: String,
        override val properties: Map<String, String> = emptyMap(),
        override val timestamp: Long = currentTimeMillis()
    ) : AnalyticsEvent() {
        override val eventName: String = "error"
    }
}
