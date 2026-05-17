//
//  CoreRestaurantListRouteHandlerImpl.swift
//  iosApp
//
//  Created on 2026-02-19
//
//  KMP-conforming wrapper for the CoreRestaurantListRouteHandlerSwift.
//  Bridges the KMP CoreRouteHandler interface to iOS-specific routing logic.

import Foundation
import shared

/// KMP CoreRouteHandler implementation for the restaurant list route.
///
/// This singleton wraps CoreRestaurantListRouteHandlerSwift and conforms to the
/// CoreRouteHandler interface, allowing it to be used with the KMP navigation system.
class CoreRestaurantListRouteHandlerImpl: CoreRouteHandler {
    static let shared = CoreRestaurantListRouteHandlerImpl()
    
    private init() {}
    
    var route: CoreRoute {
        IosAggregatorKt.createRestaurantListRoute()
    }
    
    func toRouteString() -> String {
        "restaurantList"
    }
    
    func canHandle(destination: CoreDestination) -> Bool {
        destination is CoreRestaurantListRoute
    }
    
    func destinationToRoute(destination: CoreDestination) -> CoreRoute? {
        if destination is CoreRestaurantListRoute {
            return IosAggregatorKt.createRestaurantListRoute()
        }
        return nil
    }
}
