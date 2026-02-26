package io.umain.munchies.android.di

import io.umain.munchies.android.navigation.AndroidAppRouteProviders
import io.umain.munchies.navigation.AppCoordinator
import io.umain.munchies.navigation.ScopedRouteHandler
import org.koin.java.KoinJavaComponent.getKoin

fun registerAndroidNavigationModule() {
    val koinApp = getKoin()
    val coordinator = koinApp.get<AppCoordinator>()
    val routeProviders = AndroidAppRouteProviders.create().getAllProviders()
    val routeHandlers = routeProviders.flatMap { it.getRoutes() }.filterIsInstance<ScopedRouteHandler>()
    coordinator.routeHandlers = routeHandlers
}
