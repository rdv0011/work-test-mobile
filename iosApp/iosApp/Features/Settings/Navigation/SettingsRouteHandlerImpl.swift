import Foundation
import shared

class SettingsRouteHandlerImpl: shared.RouteHandler {
    static let shared = SettingsRouteHandlerImpl()
    
    private init() {}
    
    var route: shared.Route {
        IosAggregatorKt.createSettingsRoute()
    }
    
    func toRouteString() -> String {
        "settings"
    }
    
    func canHandle(destination: shared.Destination) -> Bool {
        destination is Destination.Settings
    }
    
    func destinationToRoute(destination: shared.Destination) -> shared.Route? {
        if destination is Destination.Settings {
            return IosAggregatorKt.createSettingsRoute()
        }
        return nil
    }
}

