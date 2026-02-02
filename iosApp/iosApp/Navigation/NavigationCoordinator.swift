//
//  NavigationCoordinator.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-02-01.
//
import SwiftUI
import shared

@MainActor
class NavigationCoordinator: ObservableObject {
    @Published var path = NavigationPath()
    let coordinator: AppCoordinator
    
    init(coordinator: AppCoordinator) {
        self.coordinator = coordinator
        observeNavigationEvents()
    }
    
    private func observeNavigationEvents() {
        coordinator.navigationEvents.collect(
            collector: EventCollector(callback: { [weak self] event in
                guard let self = self else { return }
                self.handle(event: event)
            }),
            completionHandler: { error in
                if let error = error {
                    logError(tag: "Navigation", message: "Navigation collection error: \(error)")
                }
            }
        )
    }
    
    func handle(event: NavigationEvent) {
        switch event {
        case let push as NavigationEvent.Push:
            handlePush(destination: push.destination)
        case is NavigationEvent.Pop:
            if !path.isEmpty {
                path.removeLast()
            }
        case is NavigationEvent.PopToRoot:
            path = NavigationPath()
        default:
            break
        }
    }
    
    private func handlePush(destination: shared.Destination) {
        switch destination {
        case is Destination.RestaurantList:
            break
        case let detail as Destination.RestaurantDetail:
            path.append(Route.restaurantDetail(detail.restaurantId))
        default:
            break
        }
    }
}
