package io.umain.munchies.aggregator

/**
 * iOS Aggregator Module
 * 
 * This module exists solely to aggregate all feature modules into a single XCFramework for iOS.
 * It exports:
 * - core: Infrastructure (networking, logging, localization, navigation)
 * - ui-components: Reusable UI components
 * - feature-restaurant: Restaurant list and detail features
 * 
 * This approach simplifies iOS integration while maintaining clean module boundaries for Android.
 */
object IosAggregator {
    const val VERSION = "1.0.0"
}
