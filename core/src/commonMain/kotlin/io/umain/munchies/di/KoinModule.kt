package io.umain.munchies.di

import io.umain.munchies.core.localization.StringResourceProvider
import io.umain.munchies.core.localization.getStringResourceProvider
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.RouteHandler
import io.umain.munchies.navigation.RouteRegistry
import io.umain.munchies.navigation.DeepLinkParser
import io.umain.munchies.navigation.DeepLinkHandler
import io.umain.munchies.core.navigation.NavigationDispatcher
import io.umain.munchies.network.createHttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val commonModule = module {
    single { createHttpClient(get()) }
    single<StringResourceProvider> { getStringResourceProvider() }
    // Register RouteRegistry with all RouteHandlers
    single { RouteRegistry(getAll()) }
    // Register DeepLinkParser with all DeepLinkHandlers
    single { DeepLinkParser(getAll()) }
}

expect fun createAppCoordinator(): AppCoordinator

expect val platformModule: Module
expect val analyticsModule: Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(
        commonModule,
        platformModule,
        analyticsModule
    )
}

fun initKoin() = initKoin {}
