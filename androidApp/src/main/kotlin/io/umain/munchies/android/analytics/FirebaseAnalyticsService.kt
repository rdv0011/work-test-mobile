package io.umain.munchies.android.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import io.umain.munchies.core.analytics.AnalyticsEvent
import io.umain.munchies.core.analytics.AnalyticsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseAnalyticsService : AnalyticsService {
    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    override suspend fun trackEvent(event: AnalyticsEvent) {
        withContext(Dispatchers.Default) {
            val bundle = android.os.Bundle().apply {
                when (event) {
                    is AnalyticsEvent.ScreenView -> {
                        putString("screen_name", event.screenName)
                        putString("screen_class", event.screenClass)
                        event.previousScreen?.let { putString("previous_screen", it) }
                    }

                    is AnalyticsEvent.TabSwitch -> {
                        putString("tab_id", event.tabId)
                        putString("tab_name", event.tabName)
                    }

                    is AnalyticsEvent.ModalOpen -> {
                        putString("modal_name", event.modalName)
                        putString("modal_class", event.modalClass)
                    }

                    is AnalyticsEvent.ModalDismiss -> {
                        putString("modal_name", event.modalName)
                        putLong("time_spent_ms", event.timeSpentMs)
                    }

                    is AnalyticsEvent.CustomEvent -> {
                        event.properties.forEach { (k, v) -> putString(k, v) }
                    }

                    is AnalyticsEvent.ErrorEvent -> {
                        putString("error_type", event.errorType)
                        putString("error_message", event.errorMessage)
                        putString("error_context", event.errorContext)
                    }
                }
            }

            event.properties.forEach { (key, value) ->
                if (!bundle.containsKey(key)) {
                    bundle.putString(key, value)
                }
            }

            firebaseAnalytics.logEvent(event.eventName, bundle)
        }
    }

    override suspend fun setUserProperties(properties: Map<String, String>) {
        withContext(Dispatchers.Default) {
            properties.forEach { (key, value) ->
                firebaseAnalytics.setUserProperty(key, value)
            }
        }
    }

    override suspend fun clearUserProperties() {
        withContext(Dispatchers.Default) {
            firebaseAnalytics.resetAnalyticsData()
        }
    }

    override suspend fun flush() {
        withContext(Dispatchers.Default) {
            // Firebase Analytics flushes automatically, but we can force it if needed
        }
    }
}
