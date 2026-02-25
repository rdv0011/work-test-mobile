import SwiftUI
import shared

@main
struct MunchiesApp: App {
    init() {
        KoinModule_iosKt.doInitKoinIos()
        FeatureRestaurantDiKt.registerFeatureRestaurantModule()
        FeatureSettingsDiKt.registerFeatureSettingsModule()
    }
    
    private var coordinator: AppCoordinator {
        KoinModule_iosKt.getAppCoordinator()
    }
    
    var body: some Scene {
        WindowGroup {
            AppNavigationView(coordinator: coordinator)
                .onOpenURL { url in
                    handleDeepLink(url)
                }
        }
    }
    
    private func handleDeepLink(_ url: URL) {
        guard url.scheme == "munchies" else { return }
        
        // Extract components from URL
        let host = url.host ?? ""
        let path = url.path
        let pathComponents = path.split(separator: "/").map(String.init)
        
        switch host {
        case "restaurants":
            if pathComponents.isEmpty {
                // munchies://restaurants -> Restaurant List
                coordinator.navigateToScreen(destination: Destination.RestaurantList())
            } else if pathComponents.count == 1, let restaurantId = pathComponents.first {
                // munchies://restaurants/{restaurantId} -> Restaurant Detail
                coordinator.navigateToScreen(destination: Destination.RestaurantDetail(restaurantId: restaurantId))
            }
            
        case "settings":
            // munchies://settings -> Settings Tab
            coordinator.selectTab(tabId: "settings")
            
        case "modal":
            // Handle modal routes
            guard pathComponents.count >= 1 else { return }
            let modalType = pathComponents[0]
            
            switch modalType {
            case "filter":
                // munchies://modal/filter?filters=tag1,tag2 -> Filter Modal
                let queryItems = URLComponents(url: url, resolvingAgainstBaseURL: false)?.queryItems ?? []
                let filtersParam = queryItems.first(where: { $0.name == "filters" })?.value ?? ""
                let preSelectedFilters = filtersParam.isEmpty ? [] : filtersParam.split(separator: ",").map(String.init)
                coordinator.showFilterModal(preSelectedFilters: preSelectedFilters)
                
            case "submit_review":
                // munchies://modal/submit_review/{restaurantId} -> Submit Review Modal
                if pathComponents.count == 2, let restaurantId = pathComponents[safe: 1] {
                    coordinator.submitReview(restaurantId: restaurantId)
                }
                
            case "confirm":
                // munchies://modal/confirm?message=...&confirmText=...&cancelText=... -> Confirm Dialog
                let queryItems = URLComponents(url: url, resolvingAgainstBaseURL: false)?.queryItems ?? []
                let message = queryItems.first(where: { $0.name == "message" })?.value ?? "Are you sure?"
                let confirmText = queryItems.first(where: { $0.name == "confirmText" })?.value ?? "OK"
                let cancelText = queryItems.first(where: { $0.name == "cancelText" })?.value ?? "Cancel"
                coordinator.showConfirmation(message: message, confirmText: confirmText, cancelText: cancelText)
                
            case "date_picker":
                // munchies://modal/date_picker?initialDate=2026-02-25 -> Date Picker Modal
                let queryItems = URLComponents(url: url, resolvingAgainstBaseURL: false)?.queryItems ?? []
                let initialDate = queryItems.first(where: { $0.name == "initialDate" })?.value
                coordinator.showModal(destination: shared.ModalDestination.DatePicker(initialDate: initialDate))
                
            default:
                break
            }
            
        default:
            break
        }
    }
}

extension Array {
    subscript(safe index: Int) -> Element? {
        return indices.contains(index) ? self[index] : nil
    }
}
