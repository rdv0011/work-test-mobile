//
//  RestaurantRouteProvider.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-02-14.
//

import Foundation
import shared

/**
 * Restaurant feature route provider for iOS.
 *
 * Declares all routes provided by the Restaurant feature
 * and handles the creation of view model holders for each route.
 */
class RestaurantRouteProvider: RouteProvider {
    private let coordinator: AppCoordinator
    private let holderRegistry: RestaurantHolderProvider

    init(coordinator: AppCoordinator, holderRegistry: RestaurantHolderProvider) {
        self.coordinator = coordinator
        self.holderRegistry = holderRegistry
    }

    func getRoutes() -> [RouteHandler] {
        return [
            RestaurantListRouteHandlerImpl.shared,
            RestaurantDetailRouteHandlerImpl.shared
        ]
    }

    /// Get the appropriate view model holder for a route
    func getHolder(for route: shared.Route) -> AnyObject? {
        switch route {
        case _ as RestaurantListRoute:
            return holderRegistry.restaurantListHolder()
        case let r as RestaurantDetailRoute:
            return holderRegistry.restaurantDetailHolder(restaurantId: r.restaurantId)
        default:
            return nil
        }
    }
}

/**
 * Protocol for restaurant holder creation.
 *
 * Separated to allow for dependency injection of holder creation logic.
 */
protocol RestaurantHolderProvider {
    func restaurantListHolder() -> RestaurantListViewModelHolder
    func restaurantDetailHolder(restaurantId: String) -> RestaurantDetailViewModelHolder
}

/**
 * Default implementation of RestaurantHolderProvider.
 *
 * Creates view model holders by delegating to RouteHolderRegistry.
 */
struct RestaurantHolderProviderImpl: RestaurantHolderProvider {
    func restaurantListHolder() -> RestaurantListViewModelHolder {
        fatalError("Use NavigationCoordinator.restaurantListHolder() instead")
    }
    
    func restaurantDetailHolder(restaurantId: String) -> RestaurantDetailViewModelHolder {
        fatalError("Use NavigationCoordinator.restaurantDetailHolder() instead")
    }
}
