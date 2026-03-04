package io.umain.munchies.di

import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.RouteHandler
import io.umain.munchies.core.navigation.NavigationDispatcher
import io.umain.munchies.network.createHttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val commonModule = module {
    single { createHttpClient(get()) }
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
