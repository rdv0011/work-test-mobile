//
//  SettingsRouteHandlerSwift.swift
//  iosApp
//

import SwiftUI
import shared

@MainActor
class SettingsRouteHandlerSwift {
    
    private let routeRegistry: RouteRegistry
    
    init(routeRegistry: RouteRegistry) {
        self.routeRegistry = routeRegistry
    }
    
    var routeString: String {
        "settings"
    }
    
    func createHolder() -> SettingsViewModelHolder {
        let route = Route.settings
        let key = route.key
        
        let scope = routeRegistry.lifetimeFor(routeId: key) {
            FeatureSettingsIosKt.createSettingsScopeIos()
        }
        
        return SettingsViewModelHolder(scope: scope)
    }
}
