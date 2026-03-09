package io.umain.munchies.di

import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.RouteHandler
import io.umain.munchies.network.provideHttpClientEngine
import io.umain.munchies.core.navigation.NavigationDispatcher
import io.umain.munchies.logging.logInfo
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.dsl.module

private var koinCallCounter = 0

actual val platformModule = module {
    single { provideHttpClientEngine() }
}

private lateinit var koinApplication: KoinApplication

fun initKoinIos(): KoinApplication {
    logInfo("KoinModule.ios", "🚀 initKoinIos() starting...")
    val app = initKoin {
        koinApplication = this
    }
    logInfo("KoinModule.ios", "✅ Koin initialized successfully")
    return app
}

fun getKoin(): Koin = koinApplication.koin

fun debugKoinState(tag: String = "") {
    try {
        val handlers: List<RouteHandler> = koinApplication.koin.getAll<RouteHandler>()
        logInfo("KoinModule.ios", "🔍 DEBUG[$tag] Currently registered RouteHandlers: ${handlers.size}")
        handlers.forEachIndexed { i, h -> logInfo("KoinModule.ios", "  [$i] ${h::class.qualifiedName}") }
    } catch (e: Exception) {
        logInfo("KoinModule.ios", "⚠️ DEBUG[$tag] Failed to list handlers: ${e.message}")
    }
}

actual fun createAppCoordinator(): AppCoordinator {
    val koin = koinApplication.koin
    logInfo("KoinModule.ios", "🔍 Attempting to retrieve route handlers from Koin...")
    
    debugKoinState("before-getAll")
    
    val handlers: List<RouteHandler> = try {
        koin.getAll<RouteHandler>()
    } catch (e: Exception) {
        logInfo("KoinModule.ios", "⚠️ Exception during getAll<RouteHandler>(): ${e.message}")
        e.printStackTrace()
        emptyList()
    }
    
    logInfo("KoinModule.ios", "🏗️ Creating AppCoordinator with ${handlers.size} route handlers")
    handlers.forEachIndexed { index, handler ->
        logInfo("KoinModule.ios", "  [$index] ${handler::class.simpleName}")
    }
    
    if (handlers.isEmpty()) {
        logInfo("KoinModule.ios", "⚠️ WARNING: No route handlers found! This will break navigation.")
    }
    
    val coordinator = AppCoordinator(routeHandlers = handlers)
    koin.declare(coordinator)
    koin.declare(NavigationDispatcher(coordinator))
    logInfo("KoinModule.ios", "✅ AppCoordinator created successfully")
    return coordinator
}

fun getAppCoordinator(): AppCoordinator = koinApplication.koin.get()

fun getAnalyticsService(): io.umain.munchies.core.analytics.AnalyticsService {
    val service = koinApplication.koin.get<io.umain.munchies.core.analytics.AnalyticsService>()
    val callId = ++koinCallCounter
    logInfo(tag = "KoinModule.ios", message = "📦 iOS [${callId}]: Got AnalyticsService from Koin")
    return service
}

fun getKoinHttpClient() = koinApplication.koin.get<io.ktor.client.HttpClient>()

fun getKoinForIos(): Koin = koinApplication.koin

