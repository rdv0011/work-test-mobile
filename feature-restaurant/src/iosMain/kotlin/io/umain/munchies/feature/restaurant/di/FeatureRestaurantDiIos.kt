package io.umain.munchies.feature.restaurant.di

import org.koin.core.context.loadKoinModules

actual fun loadRestaurantKoinModules() {
    loadKoinModules(featureRestaurantModuleIos)
}
