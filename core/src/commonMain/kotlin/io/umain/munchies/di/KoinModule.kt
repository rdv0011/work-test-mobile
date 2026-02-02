package io.umain.munchies.di

import io.umain.munchies.navigation.AppCoordinator
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val commonModule = module {
    single { AppCoordinator() }
    // Network HttpClient provider (lazy)
    single { io.umain.munchies.network.createHttpClient(get()) }
}

expect val platformModule: Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(commonModule, platformModule)
}

fun initKoin() = initKoin {}
