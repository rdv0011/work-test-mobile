import Foundation
import shared

/// Holds the RestaurantDetailViewModel and its associated scope.
///
/// OWNERSHIP MODEL:
/// - This holder does NOT own the Koin scope
/// - RouteRegistry owns the scope and decides when it closes
/// - This holder just retains the ViewModel during its lifetime
/// - The Scope is closed by RouteRegistry.cleanup(), not by deinit
///
/// CRITICAL: The view MUST retain this holder to keep it alive.
/// If the holder is deallocated before the view disappears, the ViewModel
/// can continue to exist (since the scope is managed by RouteRegistry),
/// but it's cleaner to keep the holder alive as long as the view is alive.
///
/// LIFECYCLE:
/// 1. RouteRegistry.lifetimeFor() creates the Koin scope via factory
/// 2. This holder is created and retains the ViewModel
/// 3. View retains this holder (via @ObservedObject or similar)
/// 4. When route becomes inactive, RouteRegistry.cleanup() closes the scope
/// 5. When this holder is deallocated, scope is already closed by Registry
final class RestaurantDetailViewModelHolder: ObservableObject {
    let viewModel: RestaurantDetailViewModel
    let scope: Scope
    let restaurantId: String
    
    init(restaurantId: String, scope: Scope) {
        self.restaurantId = restaurantId
        self.scope = scope
        self.viewModel = try! scope.get(viewModel: RestaurantDetailViewModel.self)
    }
    
    deinit {
        // CRITICAL: Do NOT close the scope here.
        // RouteRegistry is responsible for closing scopes.
        // This holder just retains the ViewModel during view lifecycle.
        viewModel.close()
    }
}
