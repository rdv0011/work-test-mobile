//
//  CoreRestaurantDetailRouteHandlerSwift.swift
//  iosApp
//
//  Created on 2026-02-17
//
//  iOS-specific handler for the RestaurantDetail route.
//  Centralizes route handling logic: scope creation, view building, and navigation mapping.

import SwiftUI
import shared

/// Handler for the restaurant detail route on iOS.
///
/// This class encapsulates all route-specific logic:
/// - Creating or retrieving the scope for this route with the restaurant ID parameter
/// - Building the view to display
/// - Mapping navigation destinations to routes
///
/// By centralizing this logic, we ensure consistent navigation across the app.
@MainActor
class CoreRestaurantDetailRouteHandlerSwift {
    
    private let routeRegistry: RouteRegistry
    
    init(routeRegistry: RouteRegistry) {
        self.routeRegistry = routeRegistry
    }
    
    /// Route string used by the Swift route enum.
    /// For iOS, we use "restaurantDetail" (camelCase) to match Swift conventions.
    var routeString: String {
        "restaurantDetail"
    }
    
    /// Creates or retrieves the ViewModel holder for this route.
    ///
    /// This method owns the complete holder creation lifecycle:
    /// 1. Gets or creates a scope via routeRegistry.lifetimeFor() with the restaurantId
    /// 2. Retrieves the ViewModel from Kotlin for this scope
    /// 3. Creates and returns the holder with scope and viewModel
    ///
    /// The calling registry is responsible only for caching the holder.
    ///
    /// - Parameter restaurantId: The ID of the restaurant to display
    /// - Returns: The holder for the restaurant detail view
    func createHolder(restaurantId: String) -> RestaurantDetailViewModelHolder {
        let route = Route.restaurantDetail(restaurantId)
        let key = route.key
        
        let scope = routeRegistry.lifetimeFor(routeId: key) {
            IosAggregatorExportsKt.createRestaurantDetailScope(restaurantId: restaurantId)
        }
        
        let viewModel = IosAggregatorExportsKt.getRestaurantDetailViewModelFromFramework(restaurantId: restaurantId)
        
        return RestaurantDetailViewModelHolder(restaurantId: restaurantId, scope: scope, viewModel: viewModel)
    }
    
    /// Builds the SwiftUI view for this route.
    ///
    /// - Parameters:
    ///   - restaurantId: The ID of the restaurant to display
    ///   - holder: The view model holder containing the view models
    ///   - coordinator: The app coordinator for navigation (used for modals and deep links)
    /// - Returns: The restaurant detail view
    @ViewBuilder
    func buildView(
        restaurantId: String,
        holder: RestaurantDetailViewModelHolder,
        coordinator: CoreAppCoordinator
    ) -> some View {
        RestaurantDetailView(
            restaurantId: restaurantId,
            navigationViewModel: holder.navigationViewModel,
            viewModel: holder.viewModel,
            holder: holder
        )
    }
}
