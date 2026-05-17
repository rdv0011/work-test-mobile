import Foundation
import shared

class CoreSettingsRouteProvider: RouteProvider {
    private let coordinator: CoreAppCoordinator
    private let holderRegistry: SettingsHolderProvider

    init(coordinator: CoreAppCoordinator, holderRegistry: SettingsHolderProvider) {
        self.coordinator = coordinator
        self.holderRegistry = holderRegistry
    }

    func getRoutes() -> [CoreRouteHandler] {
        return []
    }

    func getHolder(for route: CoreRoute) -> AnyObject? {
        switch route {
        case _ as CoreSettingsRoute:
            return holderRegistry.settingsHolder()
        default:
            return nil
        }
    }
}

protocol SettingsHolderProvider {
    func settingsHolder() -> AnyObject?
}

struct SettingsHolderProviderImpl: SettingsHolderProvider {
    func settingsHolder() -> AnyObject? {
        fatalError("Use NavigationCoordinator.settingsHolder() instead")
    }
}
