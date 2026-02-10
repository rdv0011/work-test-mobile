import Foundation
import shared

/// Holds the RestaurantListViewModel and its associated scope.
///
/// OWNERSHIP MODEL:
/// - This holder does NOT own the Koin scope
/// - RouteRegistry owns the scope and decides when it closes
/// - This holder just retains the ViewModel during its lifetime
/// - The Scope is closed by RouteRegistry.cleanup(), not by deinit
///
/// LIFECYCLE:
/// 1. RouteRegistry.lifetimeFor() creates the Koin scope via factory
/// 2. This holder is created and retains the ViewModel
/// 3. View retains this holder (via @ObservedObject or similar)
/// 4. When route becomes inactive, RouteRegistry.cleanup() closes the scope
/// 5. When this holder is deallocated, scope is already closed by Registry
final class RestaurantListViewModelHolder: ObservableObject {
    let viewModel: RestaurantListViewModel
    let scope: Scope
    
    init(scope: Scope) {
        self.scope = scope
        self.viewModel = try! scope.get(viewModel: RestaurantListViewModel.self)
    }
    
    deinit {
        // CRITICAL: Do NOT close the scope here.
        // RouteRegistry is responsible for closing scopes.
        // This holder just retains the ViewModel during view lifecycle.
        viewModel.close()
    }
}
