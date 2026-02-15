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
    private let routeProviders: [RouteProvider]
    private var routeStack: [Route] = []
    
    init(
        coordinator: AppCoordinator,
        routeProviders: [RouteProvider] = []
    ) {
        self.coordinator = coordinator
        self.routeProviders = routeProviders
        self.registry = RouteHolderRegistry(coordinator: coordinator, providers: routeProviders)
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
            if !path.isEmpty {
                path.removeLast()
            }
            if !routeStack.isEmpty {
                routeStack.removeLast()
            }
            updateActiveRoutes()
            syncCleanup()
        case is NavigationEvent.PopToRoot:
            path = NavigationPath()
            routeStack.removeAll()
            activeRoutes.removeAll()
            registry.cleanup(activeRoutes: [])
        default:
            break
        }
    }
    
    private func handlePush(destination: shared.Destination) {
        for provider in routeProviders {
            let handler = provider.getRoutes().first { 
                $0.canHandle(destination: destination) 
            }
            
            if let handler = handler,
               let kmpRoute = handler.destinationToRoute(destination: destination),
               let iosRoute = convertToIOSRoute(kmpRoute) {
                routeStack.append(iosRoute)
                path.append(iosRoute)
                updateActiveRoutes()
                return
            }
        }
        
        fatalError("No route provider found for destination: \(destination)")
    }
    
    private func convertToIOSRoute(_ kmpRoute: shared.Route) -> Route? {
        switch kmpRoute {
        case _ as RestaurantListRoute:
            return .restaurantList
        case let detailRoute as RestaurantDetailRoute:
            return .restaurantDetail(detailRoute.restaurantId)
        default:
            return nil
        }
    }
    
    private func updateActiveRoutes() {
        activeRoutes.removeAll()
        for rootRoute in Route.rootRoutes {
            activeRoutes.insert(rootRoute.key)
        }
        for route in routeStack {
            activeRoutes.insert(route.key)
        }
    }
    
    func restaurantListHolder() -> RestaurantListViewModelHolder {
        registry.restaurantListHolder()
    }
    
    func restaurantDetailHolder(restaurantId: String) -> RestaurantDetailViewModelHolder {
        registry.restaurantDetailHolder(restaurantId: restaurantId)
    }
    
    private func syncCleanup() {
        registry.cleanup(activeRoutes: activeRoutes)
    }
}
