package io.umain.munchies.di

import io.umain.munchies.core.navigation.NavigationDispatcher
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.RouteHandler
import io.umain.munchies.network.provideHttpClientEngine
import io.umain.munchies.core.localization.StringResourceProvider
import io.umain.munchies.core.localization.AndroidStringResourceProvider
import io.umain.munchies.navigation.persistence.AndroidDataStorePersistence
import io.umain.munchies.navigation.persistence.NavigationPersistenceStore
import io.umain.munchies.navigation.persistence.NavigationStateRestorer
import androidx.datastore.preferences.preferencesDataStore
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import android.content.Context
import io.umain.munchies.logging.logDebug
import io.umain.munchies.logging.logError

val Context.navigationDataStore by preferencesDataStore(name = "navigation_state")

actual val platformModule = module {
    single<StringResourceProvider> { AndroidStringResourceProvider(get<Context>()) }
    single { provideHttpClientEngine() }

    single<NavigationPersistenceStore> { AndroidDataStorePersistence(get<Context>().navigationDataStore) }
    single { NavigationStateRestorer(get<NavigationPersistenceStore>()) }

    single<AppCoordinator> {
        val handlers: List<RouteHandler> = try {
            getAll()
        } catch (_: Exception) {
            emptyList()
        }
        logDebug("KoinModule.android", "Registered RouteHandlers: ${handlers.size}")
        handlers.forEachIndexed { i, h ->
            logDebug("KoinModule.android", "  [$i] ${h::class.qualifiedName}")
        }
        if (handlers.size <= 1) {
            logError("KoinModule.android", "Only one RouteHandler registered! This will break navigation. Make sure all feature modules are loaded before AppCoordinator is created.")
        }
        logDebug("KoinModule.android", "Creating AppCoordinator with persistence store")
        AppCoordinator(
            routeHandlers = handlers,
            persistenceStore = get()
        )
    }
    single { NavigationDispatcher(get()) }
}

actual fun createAppCoordinator(): AppCoordinator {
    return GlobalContext.get().get()
}
