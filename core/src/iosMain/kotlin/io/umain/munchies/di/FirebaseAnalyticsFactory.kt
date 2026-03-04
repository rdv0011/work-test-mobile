package io.umain.munchies.di

import io.umain.munchies.core.analytics.AnalyticsService
import io.umain.munchies.logging.logInfo
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSLog

private var iosAnalyticsService: IosFirebaseAnalyticsService? = null
private var callCounter = 0

@OptIn(ExperimentalForeignApi::class)
actual fun createFirebaseAnalyticsService(): AnalyticsService {
    val service = IosFirebaseAnalyticsService()
    val callId = ++callCounter
    iosAnalyticsService = service
    logInfo(tag = "FirebaseAnalyticsFactory", message = "🆕 iOS [${callId}]: Created IosFirebaseAnalyticsService")
    return service
}

@OptIn(ExperimentalForeignApi::class)
fun injectSwiftFirebaseAnalyticsService(service: AnalyticsService) {
    val wrapper = iosAnalyticsService
    val injectId = ++callCounter
    if (wrapper != null) {
        logInfo(tag = "FirebaseAnalyticsFactory", message = "💉 iOS [${injectId}]: Injecting Swift service into wrapper")
        wrapper.setSwiftService(service)
    } else {
        logInfo(tag = "FirebaseAnalyticsFactory", message = "⚠️ iOS [${injectId}]: Could not inject Swift service: wrapper not found or wrong type")
    }
}

