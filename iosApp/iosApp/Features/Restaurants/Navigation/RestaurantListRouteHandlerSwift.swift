//
//  CoreRestaurantListRouteHandlerSwift.swift
//  iosApp
//
//  Created on 2026-02-17
//
//  iOS-specific handler for the RestaurantList route.
//  Centralizes route handling logic: scope creation, view building, and navigation mapping.

import SwiftUI
import shared

/// Handler for the restaurant list route on iOS.
///
/// This class encapsulates all route-specific logic:
/// - Creating or retrieving the scope for this route
/// - Building the view to display
/// - Mapping navigation destinations to routes
///
/// By centralizing this logic, we ensure consistent navigation across the app.
@MainActor
class CoreRestaurantListRouteHandlerSwift {
    
    private let routeRegistry: RouteRegistry
    
    init(routeRegistry: RouteRegistry) {
        self.routeRegistry = routeRegistry
    }
    
    /// Route string used by the Swift route enum.
    /// For iOS, we use "restaurantList" (camelCase) to match Swift conventions.
    var routeString: String {
        "restaurantList"
    }
    
    /// Creates or retrieves the ViewModel holder for this route.
    ///
    /// This method owns the complete holder creation lifecycle:
    /// 1. Gets or creates a scope via routeRegistry.lifetimeFor()
    /// 2. Retrieves the ViewModel from Kotlin for this scope
    /// 3. Creates and returns the holder with scope and viewModel
    ///
    /// The calling registry is responsible only for caching the holder.
    ///
    /// - Returns: The holder for the restaurant list view
    func createHolder() -> RestaurantListViewModelHolder {
        let route = Route.restaurantList
        let key = route.key
        
         let scope = routeRegistry.lifetimeFor(routeId: key) {
             IosAggregatorExportsKt.createRestaurantListScope()
         }
         
         let viewModel = IosAggregatorExportsKt.getRestaurantListViewModelFromFramework()
        
        return RestaurantListViewModelHolder(scope: scope, viewModel: viewModel)
    }
    
    /// Builds the SwiftUI view for this route.
    ///
    /// - Parameters:
    ///   - holder: The view model holder containing the view models
    ///   - coordinator: The app coordinator for navigation (used for modals and deep links)
    /// - Returns: The restaurant list view
    @ViewBuilder
    func buildView(
        holder: RestaurantListViewModelHolder,
        coordinator: CoreAppCoordinator
    ) -> some View {
        RestaurantListView(
            navigationViewModel: holder.navigationViewModel,
            viewModel: holder.viewModel
        )
    }
}
