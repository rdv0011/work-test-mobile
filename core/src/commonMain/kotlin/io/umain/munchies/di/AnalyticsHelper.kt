package io.umain.munchies.di

import io.umain.munchies.core.analytics.AnalyticsService
import io.umain.munchies.core.analytics.NavigationAnalyticsListener
import io.umain.munchies.navigation.AppCoordinator
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Starts analytics tracking by creating and launching the NavigationAnalyticsListener.
 * Must be called AFTER AppCoordinator is created and available in Koin.
 * Platform-specific implementation handles listener creation with proper timing.
 */
expect fun startAnalyticsTracking()
