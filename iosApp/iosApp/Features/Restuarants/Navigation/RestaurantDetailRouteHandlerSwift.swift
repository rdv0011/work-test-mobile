//
//  RestaurantDetailRouteHandlerSwift.swift
//  iosApp
//
//  Created on 2026-02-17
//
//  iOS-specific handler for the RestaurantDetail route.
//  Centralizes route handling logic: scope creation, view building, and navigation mapping.
//  Mirrors the Android RestaurantDetailRouteHandlerAndroid pattern.

import SwiftUI
import shared

/// Handler for the restaurant detail route on iOS.
///
/// This class encapsulates all route-specific logic:
/// - Creating or retrieving the scope for this route with the restaurant ID parameter
/// - Building the view to display
/// - Mapping navigation destinations to routes
///
/// By centralizing this logic, we follow the same pattern as Android handlers,
/// making the codebase consistent and easier to extend.
@MainActor
class RestaurantDetailRouteHandlerSwift {
    
    private let commonHandler = RestaurantDetailRouteHandler()
    private let routeRegistry: RouteRegistry
    
    init(routeRegistry: RouteRegistry) {
        self.routeRegistry = routeRegistry
    }
    
    /// The route this handler manages (template with empty ID).
    var route: shared.Route {
        commonHandler.route
    }
    
    /// Route string used by the Swift route enum.
    /// For iOS, we use "restaurantDetail" (camelCase) to match Swift conventions.
    var routeString: String {
        "restaurantDetail"
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
    ///
    /// - Parameter kmpRoute: The Kotlin route to convert
    /// - Returns: The iOS route enum case if it's a RestaurantDetailRoute, nil otherwise
    func convertToIOSRoute(_ kmpRoute: shared.Route) -> Route? {
        if let detailRoute = kmpRoute as? RestaurantDetailRoute {
            return .restaurantDetail(detailRoute.restaurantId)
        }
        return nil
    }
    
    /// Creates or retrieves the ViewModel holder for this route.
    ///
    /// This method owns the complete holder creation lifecycle:
    /// 1. Gets or creates a scope via routeRegistry.lifetimeFor() with the restaurantId
    /// 2. Creates the holder with that scope
    /// 3. Returns the holder
    ///
    /// The calling registry is responsible only for caching.
    ///
    /// - Parameter restaurantId: The ID of the restaurant to display
    /// - Returns: The holder for the restaurant detail view
    func createHolder(restaurantId: String) -> RestaurantDetailViewModelHolder {
        let route = Route.restaurantDetail(restaurantId)
        let key = route.key
        
        let scope = routeRegistry.lifetimeFor(routeId: key) {
            FeatureRestaurantIosKt.createRestaurantDetailScopeIos(restaurantId: restaurantId)
        }
        
        return RestaurantDetailViewModelHolder(restaurantId: restaurantId, scope: scope)
    }
    
    /// Builds the SwiftUI view for this route.
    ///
    /// - Parameters:
    ///   - restaurantId: The ID of the restaurant to display
    ///   - holder: The view model holder containing the view model
    ///   - coordinator: The app coordinator for navigation
    /// - Returns: The restaurant detail view
    @ViewBuilder
    func buildView(
        restaurantId: String,
        holder: RestaurantDetailViewModelHolder,
        coordinator: AppCoordinator
    ) -> some View {
        RestaurantDetailView(
            restaurantId: restaurantId,
            coordinator: coordinator,
            viewModel: holder.viewModel,
            holder: holder
        )
    }
}
