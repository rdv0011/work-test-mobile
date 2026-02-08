//
//  Route.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-02-03.
//
import shared

enum Route: Hashable {
    case restaurantList
    case restaurantDetail(String)
    
    var key: String {
        switch self {
        case .restaurantList:
            return "RestaurantList"
        case .restaurantDetail(let restaurantId):
            return "RestaurantDetail_\(restaurantId)"
        }
    }
    
    var isRootRoute: Bool {
        switch self {
        case .restaurantList:
            return true
        case .restaurantDetail:
            return false
        }
    }
    
    static var rootRoutes: [Route] {
        [.restaurantList]
    }
}

