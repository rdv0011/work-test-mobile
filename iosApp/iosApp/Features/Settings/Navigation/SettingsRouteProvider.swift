//
//  SettingsRouteProvider.swift
//  iosApp
//
//  Created on 2026-02-27
//
//  Settings feature route provider for iOS.
//  Declares all routes provided by the Settings feature.

import Foundation
import shared

/**
 * Settings feature route provider for iOS.
 *
 * Declares all routes provided by the Settings feature.
 */
class SettingsRouteProvider: RouteProvider {
    private let coordinator: AppCoordinator
    private let holderProvider: SettingsHolderProvider

    init(coordinator: AppCoordinator, holderProvider: SettingsHolderProvider) {
        self.coordinator = coordinator
        self.holderProvider = holderProvider
    }

    func getRoutes() -> [RouteHandler] {
        return [
            SettingsRouteHandlerImpl.shared
        ]
    }

    /// Get the appropriate view model holder for a route
    func getHolder(for route: shared.Route) -> AnyObject? {
        switch route {
        case _ as SettingsRoute:
            return holderProvider.settingsHolder()
        default:
            return nil
        }
    }
}

/**
 * Protocol for settings holder creation.
 *
 * Separated to allow for dependency injection of holder creation logic.
 */
protocol SettingsHolderProvider {
    func settingsHolder() -> SettingsViewModelHolder
}

/**
 * Default implementation of SettingsHolderProvider.
 *
 * Creates view model holders by delegating to RouteHolderRegistry.
 */
struct SettingsHolderProviderImpl: SettingsHolderProvider {
    func settingsHolder() -> SettingsViewModelHolder {
        fatalError("Use NavigationCoordinator.settingsHolder() instead")
    }
}
