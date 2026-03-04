package io.umain.munchies.di

import io.umain.munchies.core.analytics.AnalyticsService
import io.umain.munchies.core.analytics.NavigationAnalyticsListener
import io.umain.munchies.logging.logInfo
import io.umain.munchies.navigation.AppCoordinator
import org.koin.core.module.Module
import org.koin.dsl.module

// Module-level variable to keep the listener alive during app lifetime
private var activeListener: NavigationAnalyticsListener? = null

actual val analyticsModule: Module = module {
    single<AnalyticsService> { 
        logInfo("AnalyticsModule", "🔧 Creating AnalyticsService instance")
        createFirebaseAnalyticsService()
    }
}

actual fun startAnalyticsTracking() {
    logInfo("AnalyticsModule", "🎯 startAnalyticsTracking() called")
    val koin = getKoin()
    
    logInfo("AnalyticsModule", "📥 Getting AnalyticsService from Koin...")
    val analyticsService = koin.get<AnalyticsService>()
    logInfo("AnalyticsModule", "✅ Got AnalyticsService: ${analyticsService::class.simpleName}")
    
    logInfo("AnalyticsModule", "📥 Getting AppCoordinator from Koin...")
    val coordinator = koin.get<AppCoordinator>()
    logInfo("AnalyticsModule", "✅ Got AppCoordinator")
    
    logInfo("AnalyticsModule", "🏗️ Creating NavigationAnalyticsListener...")
    val listener = NavigationAnalyticsListener(
        analyticsService = analyticsService,
        navigationStateFlow = coordinator.navigationState
    )
    logInfo("AnalyticsModule", "✅ NavigationAnalyticsListener instance created")
    
    // Store reference to prevent garbage collection during app lifetime
    activeListener = listener
    logInfo("AnalyticsModule", "📦 Listener stored in module-level variable for lifecycle management")
    
    logInfo("AnalyticsModule", "▶️ Calling listener.startTracking()...")
    listener.startTracking()
    logInfo("AnalyticsModule", "✅ Listener tracking started - coroutine should be collecting state changes")
}
