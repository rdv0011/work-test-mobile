package io.umain.munchies.feature.restaurant.di

import org.koin.core.context.loadKoinModules

/**
 * Call this after Koin init to register feature-restaurant DI module into the running Koin instance.
 */
fun registerFeatureRestaurantModule() {
    loadKoinModules(featureRestaurantModule)
}
