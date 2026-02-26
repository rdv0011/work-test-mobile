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
        guard url.scheme == DeepLinkConstants.scheme else { return }
        
        let host = url.host ?? ""
        let path = url.path
        let pathComponents = path.split(separator: "/").map(String.init)
        
        switch host {
        case DeepLinkConstants.hostRestaurants:
            handleRestaurantDeepLink(pathComponents)
            
        case DeepLinkConstants.hostSettings:
            coordinator.selectTab(tabId: DeepLinkConstants.tabIdSettings)
            
        case DeepLinkConstants.hostModal:
            handleModalDeepLink(pathComponents, url: url)
            
        default:
            break
        }
    }
    
    private func handleRestaurantDeepLink(_ pathComponents: [String]) {
        if pathComponents.isEmpty {
            // munchies://restaurants -> Restaurant List
            coordinator.navigateToScreen(destination: Destination.RestaurantList())
        } else if pathComponents.count == 1, let restaurantId = pathComponents.first {
            // munchies://restaurants/{restaurantId} -> Restaurant Detail
            coordinator.navigateToScreen(destination: Destination.RestaurantDetail(restaurantId: restaurantId))
        }
    }
    
    private func handleModalDeepLink(_ pathComponents: [String], url: URL) {
        guard pathComponents.count >= 1 else { return }
        let modalType = pathComponents[DeepLinkConstants.modalTypeIndex]
        
        switch modalType {
        case DeepLinkConstants.pathFilter:
            // munchies://modal/filter?filters=tag1,tag2 -> Filter Modal
            let queryItems = URLComponents(url: url, resolvingAgainstBaseURL: false)?.queryItems ?? []
            let filtersParam = queryItems.first(where: { $0.name == DeepLinkConstants.queryParamFilters })?.value ?? ""
            let preSelectedFilters = filtersParam.isEmpty ? [] : filtersParam.split(separator: ",").map(String.init)
            coordinator.showFilterModal(preSelectedFilters: preSelectedFilters)
            
        case DeepLinkConstants.pathSubmitReview:
            // munchies://modal/submit_review/{restaurantId} -> Submit Review Modal
            if pathComponents.count >= DeepLinkConstants.submitReviewRestaurantIdIndex + 1,
               let restaurantId = pathComponents[safe: DeepLinkConstants.submitReviewRestaurantIdIndex] {
                coordinator.submitReview(restaurantId: restaurantId)
            }
            
        case DeepLinkConstants.pathConfirm:
            // munchies://modal/confirm?message=...&confirmText=...&cancelText=... -> Confirm Dialog
            let queryItems = URLComponents(url: url, resolvingAgainstBaseURL: false)?.queryItems ?? []
            let message = queryItems.first(where: { $0.name == DeepLinkConstants.queryParamMessage })?.value ?? DeepLinkConstants.defaultConfirmMessage
            let confirmText = queryItems.first(where: { $0.name == DeepLinkConstants.queryParamConfirmText })?.value ?? DeepLinkConstants.defaultConfirmText
            let cancelText = queryItems.first(where: { $0.name == DeepLinkConstants.queryParamCancelText })?.value ?? DeepLinkConstants.defaultCancelText
            coordinator.showConfirmation(message: message, confirmText: confirmText, cancelText: cancelText)
            
        case DeepLinkConstants.pathDatePicker:
            // munchies://modal/date_picker?initialDate=2026-02-25 -> Date Picker Modal
            let queryItems = URLComponents(url: url, resolvingAgainstBaseURL: false)?.queryItems ?? []
            let initialDate = queryItems.first(where: { $0.name == DeepLinkConstants.queryParamInitialDate })?.value
            coordinator.showModal(destination: shared.ModalDestination.DatePicker(initialDate: initialDate))
            
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
