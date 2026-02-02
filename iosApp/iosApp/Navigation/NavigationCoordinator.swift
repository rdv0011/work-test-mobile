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
            collector: NavigationEventCollector(navigator: self),
            completionHandler: { error in
                if let error = error {
                    logError(tag: "Navigation", message: "Navigation collection error: \(error)")
                }
            }
        )
    }
    
    private func startObserving() {
        navigationTask = Task {
            for await event in coordinator.navigationEvents.asAsyncSequence() {
                handle(event: event)
            }
        }
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
