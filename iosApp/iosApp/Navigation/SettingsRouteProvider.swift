import Foundation
import shared

class SettingsRouteProvider: RouteProvider {
    private let coordinator: AppCoordinator
    private let holderRegistry: SettingsHolderProvider

    init(coordinator: AppCoordinator, holderRegistry: SettingsHolderProvider) {
        self.coordinator = coordinator
        self.holderRegistry = holderRegistry
    }

    func getRoutes() -> [RouteHandler] {
        return [
            SettingsRouteHandlerImpl.shared
        ]
    }

    func getHolder(for route: shared.Route) -> AnyObject? {
        switch route {
        case _ as SettingsRoute:
            return holderRegistry.settingsHolder()
        default:
            return nil
        }
    }
}

protocol SettingsHolderProvider {
    func settingsHolder() -> SettingsViewModelHolder
}

struct SettingsHolderProviderImpl: SettingsHolderProvider {
    func settingsHolder() -> SettingsViewModelHolder {
        fatalError("Use NavigationCoordinator.settingsHolder() instead")
    }
}
