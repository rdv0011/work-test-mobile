package io.umain.munchies.di

import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.RouteHandler
import io.umain.munchies.network.provideHttpClientEngine
import io.umain.munchies.core.navigation.NavigationDispatcher
import org.koin.core.context.GlobalContext
import org.koin.dsl.module

actual val platformModule = module {
    single { provideHttpClientEngine() }
}

actual fun createAppCoordinator(): AppCoordinator {
    val koin = GlobalContext.get()
    val handlers: List<RouteHandler> = try {
        koin.getAll()
    } catch (e: Exception) {
        emptyList()
    }
    val coordinator = AppCoordinator(routeHandlers = handlers)
    koin.declare(coordinator)
    koin.declare(NavigationDispatcher(coordinator))
    return coordinator
}
