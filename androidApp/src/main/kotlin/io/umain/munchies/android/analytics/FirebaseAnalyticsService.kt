package io.umain.munchies.android.analytics

import io.umain.munchies.core.analytics.AnalyticsEvent
import io.umain.munchies.core.analytics.AnalyticsService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseAnalyticsService(
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : AnalyticsService {

    private val firebaseAnalytics by lazy {
        try {
            val firebaseClass = Class.forName("com.google.firebase.ktx.Firebase")
            val analyticsProperty = firebaseClass.getMethod("getAnalytics")
            analyticsProperty.invoke(null)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun trackEvent(event: AnalyticsEvent) {
        withContext(dispatcher) {
            val analytics = firebaseAnalytics ?: return@withContext
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

                    is AnalyticsEvent.ErrorEvent -> {
                        putString("error_type", event.errorType)
                        putString("error_message", event.errorMessage)
                        putString("error_context", event.errorContext)
                    }

                    is AnalyticsEvent.CustomEvent -> {}
                }

                event.properties.forEach { (key, value) ->
                    runCatching { putString(key, value) }
                }
            }

            runCatching {
                analytics::class.java.getMethod("logEvent", String::class.java, android.os.Bundle::class.java)
                    .invoke(analytics, event.eventName, bundle)
            }
        }
    }

    override suspend fun setUserProperties(properties: Map<String, String>) {
        withContext(dispatcher) {
            val analytics = firebaseAnalytics ?: return@withContext
            properties.forEach { (key, value) ->
                runCatching {
                    analytics::class.java.getMethod("setUserProperty", String::class.java, String::class.java)
                        .invoke(analytics, key, value)
                }
            }
        }
    }

    override suspend fun clearUserProperties() {
        withContext(dispatcher) {
            val analytics = firebaseAnalytics ?: return@withContext
            runCatching {
                analytics::class.java.getMethod("setUserId", String::class.java)
                    .invoke(analytics, null)
            }
        }
    }

    override suspend fun flush() {
        withContext(dispatcher) {}
    }
}
