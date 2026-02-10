package io.umain.munchies.di

import io.umain.munchies.localization.PlatformTranslationService
import io.umain.munchies.localization.TranslationService
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.network.provideHttpClientEngine
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.dsl.module

actual val platformModule = module {
    single<TranslationService> { 
        PlatformTranslationService()
    }
    single { provideHttpClientEngine() }
}

private lateinit var koinApplication: KoinApplication

fun initKoinIos(): KoinApplication {
    return initKoin {
        koinApplication = this
    }
}

fun getKoin(): Koin = koinApplication.koin

fun getAppCoordinator(): AppCoordinator = koinApplication.koin.get()

fun getKoinHttpClient() = koinApplication.koin.get<io.ktor.client.HttpClient>()

/**
 * Get Koin instance for iOS Swift code.
 *
 * Swift cannot call `GlobalContext.get()` directly (Android-only), so this provides
 * a stable entry point for iOS code (like RouteRegistry.swift) to access Koin.
 */
fun getKoinForIos(): Koin = koinApplication.koin
