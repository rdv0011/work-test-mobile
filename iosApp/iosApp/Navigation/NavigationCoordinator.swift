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
    @Published private(set) var activeRoutes = Set<String>()
    
    let coordinator: AppCoordinator
    private let registry: RouteHolderRegistry
    
    init(coordinator: AppCoordinator) {
        self.coordinator = coordinator
        self.registry = RouteHolderRegistry(coordinator: coordinator)
        observeNavigationEvents()
    }
    
    func observeNavigationEvents() {
        Task { [weak self] in
            guard let self = self else { return }
            await self.collectNavigationEvents()
        }
    }

    private func collectNavigationEvents() async {
        for await event in asyncKotlinStream(coordinator.navigationEvents) as AsyncStream<NavigationEvent> {
            handle(event: event)
        }
    }
    
    func handle(event: NavigationEvent) {
        switch event {
        case let push as NavigationEvent.Push:
            handlePush(destination: push.destination)
        case is NavigationEvent.Pop:
            logInfo(tag: "NavigationCoordinator", message: "Pop event, path count before: \(path.count)")
            if !path.isEmpty {
                path.removeLast()
            }
            updateActiveRoutes()
            syncCleanup()
        case is NavigationEvent.PopToRoot:
            logInfo(tag: "NavigationCoordinator", message: "PopToRoot event")
            path = NavigationPath()
            activeRoutes.removeAll()
            registry.cleanup(activeRoutes: [])
        default:
            break
        }
    }
    
    private func handlePush(destination: shared.Destination) {
        switch destination {
        case is Destination.RestaurantList:
            break
        case let detail as Destination.RestaurantDetail:
            let route = Route.restaurantDetail(detail.restaurantId)
            logInfo(tag: "NavigationCoordinator", message: "Push event, adding route: \(route.key)")
            path.append(route)
            updateActiveRoutes()
        default:
            break
        }
    }
    
    private func updateActiveRoutes() {
        activeRoutes.removeAll()
        activeRoutes.insert("RestaurantList")
        for case let route as Route in path {
            activeRoutes.insert(route.key)
        }
        logInfo(tag: "NavigationCoordinator", message: "Active routes updated: \(activeRoutes.sorted().joined(separator: ", "))")
    }
    
    func restaurantListHolder() -> RestaurantListViewModelHolder {
        registry.restaurantListHolder()
    }
    
    func restaurantDetailHolder(restaurantId: String) -> RestaurantDetailViewModelHolder {
        registry.restaurantDetailHolder(restaurantId: restaurantId)
    }
    
    private func syncCleanup() {
        logInfo(tag: "NavigationCoordinator", message: "Syncing cleanup with active routes: \(activeRoutes.sorted().joined(separator: ", "))")
        registry.cleanup(activeRoutes: activeRoutes)
    }
}
