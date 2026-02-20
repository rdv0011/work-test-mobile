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
    @Published var activeTabId = "restaurants"
    @Published var tabStacks: [String: NavigationPath] = [
        "restaurants": NavigationPath(),
        "settings": NavigationPath()
    ]
    @Published private(set) var activeRoutes = Set<String>()
    @Published var modalStack: [shared.ModalRoute] = []
    @Published var showingModal: shared.ModalRoute?
    
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
        coordinator.reduceState(event: event)
        
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
        case let selectTab as NavigationEvent.SelectTab:
            handleSelectTab(selectTab.tabId)
        case let showModal as NavigationEvent.ShowModal:
            handleShowModal(showModal.destination)
        case is NavigationEvent.DismissModal:
            handleDismissModal()
        case is NavigationEvent.DismissAllModals:
            handleDismissAllModals()
        case let dismissUntil as NavigationEvent.DismissModalUntil:
            handleDismissModalUntil(dismissUntil.predicate)
        default:
            break
        }
    }
    
    private func handleSelectTab(_ tabId: String) {
        activeTabId = tabId
        if tabId == "restaurants" {
            path = tabStacks["restaurants"] ?? NavigationPath()
        } else if tabId == "settings" {
            path = tabStacks["settings"] ?? NavigationPath()
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
                updateActiveTabStack()
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
        case _ as SettingsRoute:
            return .settings
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
    
    private func updateActiveTabStack() {
        tabStacks[activeTabId] = path
    }
    
    func restaurantListHolder() -> RestaurantListViewModelHolder {
        registry.restaurantListHolder()
    }
    
    func restaurantDetailHolder(restaurantId: String) -> RestaurantDetailViewModelHolder {
        registry.restaurantDetailHolder(restaurantId: restaurantId)
    }
    
    func settingsHolder() -> SettingsViewModelHolder {
        registry.settingsHolder()
    }
    
    private func syncCleanup() {
        registry.cleanup(activeRoutes: activeRoutes)
    }
    
    private func handleShowModal(_ destination: shared.ModalDestination) {
        if let modal = destinationToModalRoute(destination) {
            modalStack.append(modal)
            showingModal = modal
            return
        }
        
        fatalError("Unable to convert ModalDestination to ModalRoute: \(destination)")
    }
    
    private func destinationToModalRoute(_ destination: shared.ModalDestination) -> shared.ModalRoute? {
        switch destination {
        case let filter as shared.ModalDestination.Filter:
            return FilterModalRoute(preSelectedFilters: filter.preSelectedFilters)
        case let review as shared.ModalDestination.SubmitReviewModal:
            return SubmitReviewModalRoute(restaurantId: review.restaurantId)
        case let confirm as shared.ModalDestination.ConfirmAction:
            return ConfirmActionModalRoute(message: confirm.message, confirmText: confirm.confirmText, cancelText: confirm.cancelText)
        case let picker as shared.ModalDestination.DatePicker:
            return DatePickerModalRoute(initialDate: picker.initialDate)
        default:
            return nil
        }
    }
    
    private func handleDismissModal() {
        if !modalStack.isEmpty {
            modalStack.removeLast()
        }
        showingModal = modalStack.last
    }
    
    private func handleDismissAllModals() {
        modalStack.removeAll()
        showingModal = nil
    }
    
    private func handleDismissModalUntil(_ predicate: @escaping (shared.ModalRoute) -> KotlinBoolean) {
        while !modalStack.isEmpty {
            if let last = modalStack.last {
                if predicate(last).boolValue {
                    break
                }
            }
            modalStack.removeLast()
        }
        showingModal = modalStack.last
    }
}
