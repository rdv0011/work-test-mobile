package io.umain.munchies.di

import io.umain.munchies.core.analytics.AnalyticsService
import io.umain.munchies.core.analytics.NoOpAnalyticsService

actual fun createFirebaseAnalyticsService(): AnalyticsService {
    // Try to load FirebaseAnalyticsService from androidApp module
    // Fall back to NoOp if not available (e.g., during core compilation)
    return try {
        val clazz = Class.forName("io.umain.munchies.android.analytics.FirebaseAnalyticsService")
        clazz.getDeclaredConstructor().newInstance() as AnalyticsService
    } catch (e: Exception) {
        NoOpAnalyticsService()
    }
}
