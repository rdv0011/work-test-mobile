//
//  NavigationStateObserver.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-01-31.
//
import shared

@MainActor
class NavigationStateObserver: ObservableObject {
    @Published var currentDestination: Destination
    let coordinator: AppCoordinator
    
    init(coordinator: AppCoordinator) {
        self.coordinator = coordinator
        // Initialize with some default; will update once collect runs
        self.currentDestination = coordinator.currentDestination.value as? shared.Destination
        ?? Destination.RestaurantList()

        // Collect the SharedStateFlow updates
        coordinator.currentDestination.collect(
            collector: DestinationCollector(observer: self),
            completionHandler: { error in
                if let error = error {
                    print("Navigation collection error: \(error)")
                }
            }
        )
    }
}
