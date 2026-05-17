//
//  CoreRestaurantDetailRouteHandlerImpl.swift
//  iosApp
//
//  Created on 2026-02-19
//
//  KMP-conforming wrapper for the CoreRestaurantDetailRouteHandlerSwift.
//  Bridges the KMP CoreRouteHandler interface to iOS-specific routing logic.

import Foundation
import shared

/// KMP CoreRouteHandler implementation for the restaurant detail route.
///
/// This singleton wraps CoreRestaurantDetailRouteHandlerSwift and conforms to the
/// CoreRouteHandler interface, allowing it to be used with the KMP navigation system.
class CoreRestaurantDetailRouteHandlerImpl: CoreRouteHandler {
    static let shared = CoreRestaurantDetailRouteHandlerImpl()
    
    private init() {}
    
    var route: CoreRoute {
        IosAggregatorKt.createRestaurantDetailRoute(restaurantId: "")
    }
    
    func toRouteString() -> String {
        "restaurantDetail"
    }
    
    func canHandle(destination: CoreDestination) -> Bool {
        destination is CoreRestaurantDetailRoute
    }
    
    func destinationToRoute(destination: CoreDestination) -> CoreRoute? {
        if let detail = destination as? CoreRestaurantDetailRoute {
            return IosAggregatorKt.createRestaurantDetailRoute(restaurantId: detail.restaurantId)
        }
        return nil
    }
}
