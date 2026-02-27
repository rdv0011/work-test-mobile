//
//  SettingsRouteHandlerSwift.swift
//  iosApp
//

import SwiftUI
import shared

@MainActor
class SettingsRouteHandlerSwift {
    
    private let commonHandler = SettingsRouteHandler()
    private let routeRegistry: RouteRegistry
    
    init(routeRegistry: RouteRegistry) {
        self.routeRegistry = routeRegistry
    }
    
    var route: shared.Route {
        commonHandler.route
    }
    
    var routeString: String {
        "settings"
    }
    
    func canHandle(destination: shared.Destination) -> Bool {
        commonHandler.canHandle(destination: destination)
    }
    
    func destinationToRoute(destination: shared.Destination) -> shared.Route? {
        commonHandler.destinationToRoute(destination: destination)
    }
    
    func convertToIOSRoute(_ kmpRoute: shared.Route) -> Route? {
        if kmpRoute is SettingsRoute {
            return .settings
        }
        return nil
    }
    
    func createHolder() -> SettingsViewModelHolder {
        let route = Route.settings
        let key = route.key
        
        let scope = routeRegistry.lifetimeFor(routeId: key) {
            FeatureSettingsIosKt.createSettingsScopeIos()
        }
        
        return SettingsViewModelHolder(scope: scope)
    }
    
    @ViewBuilder
    func buildView(
        holder: SettingsViewModelHolder,
        coordinator: AppCoordinator
    ) -> some View {
        SettingsView(viewModel: holder.viewModel)
    }
}
