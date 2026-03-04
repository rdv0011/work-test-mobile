package io.umain.munchies.di

import io.umain.munchies.localization.PlatformTranslationService
import io.umain.munchies.localization.TranslationService
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.RouteHandler
import io.umain.munchies.network.provideHttpClientEngine
import org.koin.core.context.GlobalContext
import org.koin.dsl.module

actual val platformModule = module {
    single<TranslationService> { 
        PlatformTranslationService()
    }
    single { provideHttpClientEngine() }
}

actual fun createAppCoordinator(): AppCoordinator {
    val koin = GlobalContext.get()
    val handlers: List<RouteHandler> = koin.getAll()
    val coordinator = AppCoordinator(routeHandlers = handlers)
    return coordinator
}
