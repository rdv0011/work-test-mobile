//
//  RestaurantListRouteHandlerSwift.swift
//  iosApp
//
//  Created on 2026-02-17
//
//  iOS-specific handler for the RestaurantList route.
//  Centralizes route handling logic: scope creation, view building, and navigation mapping.
//  Mirrors the Android RestaurantListRouteHandlerAndroid pattern.

import SwiftUI
import shared

/// Handler for the restaurant list route on iOS.
///
/// This class encapsulates all route-specific logic:
/// - Creating or retrieving the scope for this route
/// - Building the view to display
/// - Mapping navigation destinations to routes
///
/// By centralizing this logic, we follow the same pattern as Android handlers,
/// making the codebase consistent and easier to extend.
class RestaurantListRouteHandlerSwift {
    
    private let commonHandler = RestaurantListRouteHandler()
    private let routeRegistry: RouteRegistry
    
    init(routeRegistry: RouteRegistry = RouteRegistry()) {
        self.routeRegistry = routeRegistry
    }
    
    /// The route this handler manages.
    var route: shared.Route {
        commonHandler.route
    }
    
    /// Route string used by the Swift route enum.
    /// For iOS, we use "restaurantList" (camelCase) to match Swift conventions.
    var routeString: String {
        "restaurantList"
    }
    
    /// Determines if this handler can process the given destination.
    func canHandle(destination: shared.Destination) -> Bool {
        commonHandler.canHandle(destination: destination)
    }
    
    /// Converts a destination to a Route instance.
    func destinationToRoute(destination: shared.Destination) -> shared.Route? {
        commonHandler.destinationToRoute(destination: destination)
    }
    
    /// Converts a KMP Route to the iOS-specific Route enum.
    func convertToIOSRoute(_ kmpRoute: shared.Route) -> Route? {
        if kmpRoute is RestaurantListRoute {
            return .restaurantList
        }
        return nil
    }
    
    /// Creates or retrieves the ViewModel holder for this route.
    ///
    /// This method owns the complete holder creation lifecycle:
    /// 1. Gets or creates a scope via routeRegistry.lifetimeFor()
    /// 2. Creates the holder with that scope
    /// 3. Returns the holder
    ///
    /// The calling registry is responsible only for caching.
    ///
    /// - Returns: The holder for the restaurant list view
    func createHolder() -> RestaurantListViewModelHolder {
        let route = Route.restaurantList
        let key = route.key
        
        let scope = routeRegistry.lifetimeFor(routeId: key) {
            FeatureRestaurantIosKt.createRestaurantListScopeIos()
        }
        
        return RestaurantListViewModelHolder(scope: scope)
    }
    
    /// Builds the SwiftUI view for this route.
    ///
    /// - Parameters:
    ///   - holder: The view model holder containing the view model
    ///   - coordinator: The app coordinator for navigation
    /// - Returns: The restaurant list view
    @ViewBuilder
    func buildView(
        holder: RestaurantListViewModelHolder,
        coordinator: AppCoordinator
    ) -> some View {
        RestaurantListView(
            coordinator: coordinator,
            viewModel: holder.viewModel
        )
    }
}
