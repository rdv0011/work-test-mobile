package io.umain.munchies.di

import io.umain.munchies.core.analytics.AnalyticsEvent
import io.umain.munchies.core.analytics.AnalyticsService
import io.umain.munchies.logging.logInfo
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSLog

private var instanceCounter = 0

@OptIn(ExperimentalForeignApi::class)
class IosFirebaseAnalyticsService : AnalyticsService {
     
    private var swiftService: AnalyticsService? = null
    private val instanceId = ++instanceCounter
    
    fun setSwiftService(service: AnalyticsService) {
        swiftService = service
        logInfo(tag = "IosFirebaseAnalyticsService", message = "✅ iOS [$instanceId]: Swift FirebaseAnalyticsService injected successfully")
    }
    
    override suspend fun trackEvent(event: AnalyticsEvent) {
        val service = swiftService
        if (service != null) {
            logInfo(tag = "IosFirebaseAnalyticsService", message = "📤 iOS [$instanceId]: trackEvent(${event.eventName})")
            service.trackEvent(event)
        } else {
            logInfo(tag = "IosFirebaseAnalyticsService", message = "⚠️ iOS [$instanceId]: Swift FirebaseAnalyticsService not initialized yet for event ${event.eventName}")
        }
    }
    
    override suspend fun setUserProperties(properties: Map<String, String>) {
        val service = swiftService
        if (service != null) {
            service.setUserProperties(properties)
        } else {
            logInfo(tag = "IosFirebaseAnalyticsService", message = "⚠️ iOS [$instanceId]: Swift FirebaseAnalyticsService not initialized yet")
        }
    }
    
    override suspend fun clearUserProperties() {
        val service = swiftService
        if (service != null) {
            service.clearUserProperties()
        } else {
            logInfo(tag = "IosFirebaseAnalyticsService", message = "⚠️ iOS [$instanceId]: Swift FirebaseAnalyticsService not initialized yet")
        }
    }
    
    override suspend fun flush() {
        val service = swiftService
        if (service != null) {
            service.flush()
        } else {
            logInfo(tag = "IosFirebaseAnalyticsService", message = "⚠️ iOS [$instanceId]: Swift FirebaseAnalyticsService not initialized yet")
        }
    }
}
