//
//  CoreSettingsRouteHandlerSwift.swift
//  iosApp
//

import SwiftUI
import shared

@MainActor
class CoreSettingsRouteHandlerSwift {
    
    private let commonHandler = CoreSettingsRouteHandler()
    private let routeRegistry: RouteRegistry
    
    init(routeRegistry: RouteRegistry) {
        self.routeRegistry = routeRegistry
    }
    
    var route: CoreRoute {
        IosAggregatorKt.createCoreSettingsRoute()
    }
    
    var routeString: String {
        "settings"
    }
    
    func canHandle(destination: CoreDestination) -> Bool {
        commonHandler.canHandle(destination: destination)
    }
    
    func destinationToRoute(destination: CoreDestination) -> CoreRoute? {
        commonHandler.destinationToRoute(destination: destination)
    }
    
    func convertToIOSRoute(_ kmpRoute: CoreRoute) -> Route? {
        if kmpRoute is CoreSettingsRoute {
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
         holder: Feature_settingsSettingsViewModelHolder,
         coordinator: CoreAppCoordinator
     ) -> some View {
         SettingsView(navigationViewModel: holder.navigationViewModel, viewModel: holder.viewModel)
     }
}
