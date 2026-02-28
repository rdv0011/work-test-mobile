//
//  RestaurantListRouteHandlerImpl.swift
//  iosApp
//
//  Created on 2026-02-19
//
//  KMP-conforming wrapper for the RestaurantListRouteHandlerSwift.
//  Bridges the KMP RouteHandler interface to iOS-specific routing logic.

import Foundation
import shared

/// KMP RouteHandler implementation for the restaurant list route.
///
/// This singleton wraps RestaurantListRouteHandlerSwift and conforms to the
/// shared.RouteHandler interface, allowing it to be used with the KMP navigation system.
class RestaurantListRouteHandlerImpl: shared.RouteHandler {
    static let shared = RestaurantListRouteHandlerImpl()
    
    private init() {}
    
    var route: shared.Route {
        IosAggregatorKt.createRestaurantListRoute()
    }
    
    func toRouteString() -> String {
        "restaurantList"
    }
    
    func canHandle(destination: shared.Destination) -> Bool {
        destination is Destination.RestaurantList
    }
    
    func destinationToRoute(destination: shared.Destination) -> shared.Route? {
        if destination is Destination.RestaurantList {
            return IosAggregatorKt.createRestaurantListRoute()
        }
        return nil
    }
}
