package io.umain.munchies.di

import io.umain.munchies.localization.TranslationService
import io.umain.munchies.navigation.AppCoordinator
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val commonModule = module {
    single { AppCoordinator() }
}

expect val platformModule: Module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(commonModule, platformModule)
}

fun initKoin() = initKoin {}
