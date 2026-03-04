package io.umain.munchies.di

import io.umain.munchies.core.analytics.AnalyticsService
import org.koin.core.module.Module
import org.koin.dsl.module

actual val analyticsModule: Module = module {
    single<AnalyticsService> { createFirebaseAnalyticsService() }
}

actual fun startAnalyticsTracking() {
    // Android handles NavigationAnalyticsListener creation in MainActivity.onCreate()
    // This function is here for KMP expect/actual contract but is not used on Android
}
