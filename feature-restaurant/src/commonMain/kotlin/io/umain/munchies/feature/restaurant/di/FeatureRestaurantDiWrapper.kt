package io.umain.munchies.feature.restaurant.di

/**
 * Thin wrapper to expose a stable symbol for iOS/ObjC interop.
 * Swift can call `FeatureRestaurantDiWrapperKt.doRegisterFeatureRestaurantModule()`
 * if the generated name for registerFeatureRestaurantModule() is not found.
 */
fun doRegisterFeatureRestaurantModule() {
    registerFeatureRestaurantModule()
}
