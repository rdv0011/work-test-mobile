import Foundation
import shared

/// Swift extension to retrieve typed ViewModels from Koin Scope.
///
/// Koin's Scope class is defined in Kotlin, but Swift cannot call generic functions
/// with reified type parameters directly. This extension provides type-safe syntax
/// by bridging to the non-generic Kotlin helpers.
extension Scope {
    /// Retrieve a ViewModel from this scope by type.
    ///
    /// Usage:
    /// ```swift
    /// let viewModel = try! scope.get(viewModel: RestaurantDetailViewModel.self)
    /// ```
    func get<T>(viewModel type: T.Type) -> T {
        let kotlinType = type as! AnyClass
        let vmObject = self.getViewModelTyped(type: kotlinType)
        return vmObject as! T
    }
    
    /// Retrieve a ViewModel from this scope with parameters.
    ///
    /// Usage:
    /// ```swift
    /// let viewModel = try! scope.get(
    ///     viewModel: RestaurantDetailViewModel.self,
    ///     parameters: restaurantId
    /// )
    /// ```
    func get<T>(viewModel type: T.Type, parameters: Any...) -> T {
        let kotlinType = type as! AnyClass
        let vmObject = self.getViewModelTyped(type: kotlinType, parameters: parameters)
        return vmObject as! T
    }
}
