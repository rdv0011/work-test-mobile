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
    @Published var activeTabId = "restaurants"
    @Published var tabStacks: [String: NavigationPath] = [
        "restaurants": NavigationPath(),
        "settings": NavigationPath()
    ]
    @Published private(set) var activeRoutes = Set<String>()
    @Published var modalStack: [shared.ModalRoute] = []
    @Published var showingModal: shared.ModalRoute?
    @Published private(set) var showingModalKey: String? = nil
    
    let coordinator: AppCoordinator
    private let registry: RouteHolderRegistry
    private let routeProviders: [RouteProvider]
    private var routeStack: [Route] = []
    private var isListenerActive = false
    
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
            print("🗂️  DEBUG: NavigationCoordinator starting to observe navigation events")
            await self.collectNavigationEvents()
        }
    }

    private func collectNavigationEvents() async {
        isListenerActive = true
        print("🗂️  DEBUG: NavigationCoordinator listener is now ACTIVE")
        for await event in asyncKotlinStream(coordinator.navigationEvents) as AsyncStream<NavigationEvent> {
            handle(event: event)
        }
    }
    
    func processPendingDeepLink(_ url: URL) {
        print("🔗 DEBUG: processPendingDeepLink called with: \(url), listenerActive=\(isListenerActive)")
        
        guard url.scheme == DeepLinkConstants().SCHEME else {
            print("🔗 DEBUG: Invalid scheme: \(url.scheme ?? "nil")")
            return
        }
        
        let host = url.host ?? ""
        let path = url.path
        let pathComponents = path.split(separator: "/").map(String.init)
        
        print("🔗 DEBUG: Parsed URL - host='\(host)', path='\(path)', components=\(pathComponents)")
        
        var queryParams: [String: String] = [:]
        if let components = URLComponents(url: url, resolvingAgainstBaseURL: false) {
            components.queryItems?.forEach { item in
                queryParams[item.name] = item.value ?? ""
            }
        }
        
        print("🔗 DEBUG: Calling DeepLinkProcessor.processDeepLink with host='\(host)', pathSegments=\(pathComponents), queryParams=\(queryParams)")
        
        DeepLinkProcessor.shared.processDeepLink(
            host: host,
            pathSegments: pathComponents,
            queryParams: queryParams,
            coordinator: coordinator
        )
        
        print("🔗 DEBUG: DeepLinkProcessor.processDeepLink completed")
    }
    
    func handle(event: NavigationEvent) {
        print("🗂️  DEBUG: NavigationCoordinator.handle(event: \(type(of: event)))")
        coordinator.reduceState(event: event)
        
        switch event {
        case let push as NavigationEvent.Push:
            print("🗂️  DEBUG: Handling Push for destination: \(push.destination)")
            handlePush(destination: push.destination)
         case is NavigationEvent.Pop:
             print("🗂️  DEBUG: Handling Pop")
              if !routeStack.isEmpty {
                  routeStack.removeLast()
              }
              recomputeTabStacks()
              updateActiveRoutes()
              syncCleanup()
          case is NavigationEvent.PopToRoot:
              print("🗂️  DEBUG: Handling PopToRoot")
              routeStack.removeAll()
              recomputeTabStacks()
              activeRoutes.removeAll()
              registry.cleanup(activeRoutes: [])
         case let selectTab as NavigationEvent.SelectTab:
             print("🗂️  DEBUG: Handling SelectTab(\(selectTab.tabId))")
             handleSelectTab(selectTab.tabId)
         case let showModal as NavigationEvent.ShowModal:
             print("🗂️  DEBUG: Handling ShowModal for \(type(of: showModal.destination))")
             handleShowModal(showModal.destination)
         case is NavigationEvent.DismissModal:
             print("🗂️  DEBUG: Handling DismissModal")
             handleDismissModal()
         case is NavigationEvent.DismissAllModals:
             print("🗂️  DEBUG: Handling DismissAllModals")
             handleDismissAllModals()
         case let dismissUntil as NavigationEvent.DismissModalUntil:
             print("🗂️  DEBUG: Handling DismissModalUntil")
             handleDismissModalUntil(dismissUntil.predicate)
         default:
             print("🗂️  DEBUG: Unhandled event type: \(type(of: event))")
             break
        }
    }
    
     private func handleSelectTab(_ tabId: String) {
         let previousTabId = activeTabId
         activeTabId = tabId
         updateActiveRoutes()
         syncCleanup()
     }
    
     private func handlePush(destination: shared.Destination) {
         print("🗂️  DEBUG: handlePush - looking for route provider to handle \(type(of: destination))")
         for (index, provider) in routeProviders.enumerated() {
             print("🗂️  DEBUG:   Provider \(index): \(type(of: provider))")
             let handler = provider.getRoutes().first { 
                 $0.canHandle(destination: destination) 
             }
             
             if let handler = handler {
                 print("🗂️  DEBUG:   ✓ Found handler: \(type(of: handler))")
                 if let kmpRoute = handler.destinationToRoute(destination: destination) {
                     print("🗂️  DEBUG:     ✓ Converted to KMP route: \(kmpRoute)")
                     if let iosRoute = convertToIOSRoute(kmpRoute) {
                         print("🗂️  DEBUG:     ✓ Converted to iOS route: \(iosRoute)")
                         routeStack.append(iosRoute)
                         recomputeTabStacks()
                         updateActiveRoutes()
                         syncCleanup()
                         return
                     } else {
                         print("🗂️  DEBUG:     ✗ Failed to convert KMP route to iOS route")
                     }
                 } else {
                     print("🗂️  DEBUG:     ✗ Handler returned nil for destinationToRoute")
                 }
             }
         }
         
         print("🗂️  ERROR: No route provider found for destination: \(destination)")
         fatalError("No route provider found for destination: \(destination)")
     }
    
     private func convertToIOSRoute(_ kmpRoute: shared.Route) -> Route? {
         print("🗂️  DEBUG: convertToIOSRoute - converting \(type(of: kmpRoute))")
         switch kmpRoute {
         case _ as RestaurantListRoute:
             print("🗂️  DEBUG:   ✓ RestaurantListRoute -> .restaurantList")
             return .restaurantList
         case let detailRoute as RestaurantDetailRoute:
             print("🗂️  DEBUG:   ✓ RestaurantDetailRoute -> .restaurantDetail(\(detailRoute.restaurantId))")
             return .restaurantDetail(detailRoute.restaurantId)
         case _ as SettingsRoute:
             print("🗂️  DEBUG:   ✓ SettingsRoute -> .settings")
             return .settings
         default:
             print("🗂️  DEBUG:   ✗ Unknown route type: \(type(of: kmpRoute))")
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
    
     private func recomputeTabStacks() {
         var newTabStacks: [String: NavigationPath] = [
             "restaurants": NavigationPath(),
             "settings": NavigationPath()
         ]
         
         for route in routeStack {
             let tabId = tabIdForRoute(route)
             newTabStacks[tabId]?.append(route)
         }
         
         tabStacks = newTabStacks
     }
     
     private func tabIdForRoute(_ route: Route) -> String {
         switch route {
         case .restaurantList:
             return "restaurants"
         case .restaurantDetail:
             return "restaurants"
         case .settings:
             return "settings"
         }
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
             showingModalKey = modal.key
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
         showingModalKey = modalStack.last?.key
     }
    
     private func handleDismissAllModals() {
         modalStack.removeAll()
         showingModal = nil
         showingModalKey = nil
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
         showingModalKey = modalStack.last?.key
     }
}
