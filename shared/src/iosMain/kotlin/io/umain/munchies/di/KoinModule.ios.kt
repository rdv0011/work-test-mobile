package io.umain.munchies.di

import io.umain.munchies.localization.PlatformTranslationService
import io.umain.munchies.localization.TranslationService
import io.umain.munchies.navigation.AppCoordinator
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.dsl.module

actual val platformModule = module {
    single<TranslationService> { 
        PlatformTranslationService()
    }
}

private lateinit var koinApplication: KoinApplication

fun initKoinIos(): KoinApplication {
    return initKoin {
        koinApplication = this
    }
}

fun getKoin(): Koin = koinApplication.koin

fun getAppCoordinator(): AppCoordinator = koinApplication.koin.get()
