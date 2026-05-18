//
//  Route.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-02-03.
//
import shared

struct RouteConstants {
    static let KEY_RESTAURANT_LIST = "RestaurantList"
    static let KEY_RESTAURANT_DETAIL_PREFIX = "RestaurantDetail_"
    
    var KEY_RESTAURANT_LIST: String { Self.KEY_RESTAURANT_LIST }
    var KEY_RESTAURANT_DETAIL_PREFIX: String { Self.KEY_RESTAURANT_DETAIL_PREFIX }
}

enum Route: Hashable {
    case restaurantList
    case restaurantDetail(String)
    case settings
    
    var key: String {
        switch self {
        case .restaurantList:
            return Self.KEY_RESTAURANT_LIST
        case .restaurantDetail(let restaurantId):
            return "\(Self.KEY_RESTAURANT_DETAIL_PREFIX)\(restaurantId)"
        case .settings:
            return Self.KEY_SETTINGS
        }
    }
    
    var isRootRoute: Bool {
        switch self {
        case .restaurantList:
            return true
        case .restaurantDetail:
            return false
        case .settings:
            return true
        }
    }
    
    static var rootRoutes: [Route] {
        [.restaurantList, .settings]
    }
    
    static let KEY_RESTAURANT_LIST = RouteConstants().KEY_RESTAURANT_LIST
    static let KEY_RESTAURANT_DETAIL_PREFIX = RouteConstants().KEY_RESTAURANT_DETAIL_PREFIX
    static let KEY_SETTINGS = "settings"
}


