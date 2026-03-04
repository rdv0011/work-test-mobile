package io.umain.munchies.core.analytics

import io.umain.munchies.core.util.currentTimeMillis

sealed class AnalyticsEvent {
    abstract val eventName: String
    abstract val properties: Map<String, String>
    abstract val timestamp: Long

    data class ScreenView(
        val screenName: String,
        val screenClass: String,
        val previousScreen: String? = null,
        override val properties: Map<String, String> = emptyMap(),
        override val timestamp: Long = currentTimeMillis()
    ) : AnalyticsEvent() {
        override val eventName: String = "screen_view"
    }

    data class TabSwitch(
        val tabId: String,
        val tabName: String,
        override val properties: Map<String, String> = emptyMap(),
        override val timestamp: Long = currentTimeMillis()
    ) : AnalyticsEvent() {
        override val eventName: String = "tab_switch"
    }

    data class ModalOpen(
        val modalName: String,
        val modalClass: String,
        override val properties: Map<String, String> = emptyMap(),
        override val timestamp: Long = currentTimeMillis()
    ) : AnalyticsEvent() {
        override val eventName: String = "modal_open"
    }

    data class ModalDismiss(
        val modalName: String,
        val timeSpentMs: Long,
        override val properties: Map<String, String> = emptyMap(),
        override val timestamp: Long = currentTimeMillis()
    ) : AnalyticsEvent() {
        override val eventName: String = "modal_dismiss"
    }

    data class CustomEvent(
        override val eventName: String,
        override val properties: Map<String, String> = emptyMap(),
        override val timestamp: Long = currentTimeMillis()
    ) : AnalyticsEvent()

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

