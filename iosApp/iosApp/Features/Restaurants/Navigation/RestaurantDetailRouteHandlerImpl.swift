//
//  RestaurantDetailRouteHandlerImpl.swift
//  iosApp
//
//  Created on 2026-02-19
//
//  KMP-conforming wrapper for the RestaurantDetailRouteHandlerSwift.
//  Bridges the KMP RouteHandler interface to iOS-specific routing logic.

import Foundation
import shared

/// KMP RouteHandler implementation for the restaurant detail route.
///
/// This singleton wraps RestaurantDetailRouteHandlerSwift and conforms to the
/// shared.RouteHandler interface, allowing it to be used with the KMP navigation system.
class RestaurantDetailRouteHandlerImpl: shared.RouteHandler {
    static let shared = RestaurantDetailRouteHandlerImpl()
    
    private init() {}
    
    var route: shared.Route {
        IosAggregatorKt.createRestaurantDetailRoute(restaurantId: "")
    }
    
    func toRouteString() -> String {
        "restaurantDetail"
    }
    
    func canHandle(destination: shared.Destination) -> Bool {
        destination is Destination.RestaurantDetail
    }
    
    func destinationToRoute(destination: shared.Destination) -> shared.Route? {
        if let detail = destination as? Destination.RestaurantDetail {
            return IosAggregatorKt.createRestaurantDetailRoute(restaurantId: detail.restaurantId)
        }
        return nil
    }
}
