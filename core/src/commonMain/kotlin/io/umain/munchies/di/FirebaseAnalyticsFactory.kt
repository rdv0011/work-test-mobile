package io.umain.munchies.di

import io.umain.munchies.core.analytics.AnalyticsService

/**
 * Factory for creating platform-specific Firebase Analytics service implementations.
 * 
 * On Android: Returns FirebaseAnalyticsService from androidApp module
 * On iOS: Returns Swift FirebaseAnalyticsService via KMP interop
 */
expect fun createFirebaseAnalyticsService(): AnalyticsService
