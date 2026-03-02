package io.umain.munchies.android.di

import io.umain.munchies.android.analytics.FirebaseAnalyticsService
import io.umain.munchies.core.analytics.AnalyticsService
import io.umain.munchies.core.analytics.NavigationAnalyticsListener
import io.umain.munchies.navigation.AppCoordinator
import org.koin.dsl.module

fun analyticsModule(coordinator: AppCoordinator) = module {
    single<AnalyticsService> { FirebaseAnalyticsService() }
    single {
        NavigationAnalyticsListener(
            analyticsService = get(),
            navigationStateFlow = coordinator.navigationState
        )
    }
}
