package io.umain.munchies.di

import io.umain.munchies.localization.PlatformTranslationService
import io.umain.munchies.localization.TranslationService
import io.umain.munchies.network.provideHttpClientEngine
import org.koin.dsl.module

actual val platformModule = module {
    single<TranslationService> { 
        PlatformTranslationService()
    }
    single { provideHttpClientEngine() }
}
