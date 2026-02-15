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
            return Self.KEY_RESTAURANT_LIST
        case .restaurantDetail(let restaurantId):
            return "\(Self.KEY_RESTAURANT_DETAIL_PREFIX)\(restaurantId)"
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
    
    static let KEY_RESTAURANT_LIST = "RestaurantList"
    static let KEY_RESTAURANT_DETAIL_PREFIX = "RestaurantDetail_"
}

