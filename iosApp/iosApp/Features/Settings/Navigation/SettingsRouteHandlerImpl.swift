import Foundation
import shared

class CoreSettingsRouteHandlerImpl: CoreRouteHandler {
    static let shared = CoreSettingsRouteHandlerImpl()
    
    private init() {}
    
    var route: CoreRoute {
        IosAggregatorKt.createSettingsRoute()
    }
    
    func toRouteString() -> String {
        "settings"
    }
    
    func canHandle(destination: CoreDestination) -> Bool {
        destination is CoreSettingsRoute
    }
    
    func destinationToRoute(destination: CoreDestination) -> CoreRoute? {
        if destination is CoreSettingsRoute {
            return IosAggregatorKt.createSettingsRoute()
        }
        return nil
    }
}

