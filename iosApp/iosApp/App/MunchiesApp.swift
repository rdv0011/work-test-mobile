import SwiftUI
import shared

@main
struct MunchiesApp: App {
    init() {}
    
    @State private var pendingDeepLinkUrl: URL?
    private let coordinator = CoreAppCoordinator(
        initialState: CoreNavigationState(
            modalStack: [],
            tabNavigation: CoreTabNavigationState(
                tabDefinitions: [],
                activeTabId: "restaurants",
                stacksByTab: [:],
                navigationDirection: CoreNavigationDirection.forward
            ),
            originDeepLink: nil
        ),
        routeHandlers: [],
        persistenceStore: nil
    )
    
    var body: some Scene {
        WindowGroup {
            AppNavigationView(coordinator: coordinator, pendingDeepLinkUrl: $pendingDeepLinkUrl)
                .onOpenURL { url in
                    handleDeepLink(url)
                }
        }
    }
    
    private func handleDeepLink(_ url: URL) {
        pendingDeepLinkUrl = url
    }
}

