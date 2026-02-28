import SwiftUI
import shared

struct AppNavigationView: View {
    @StateObject private var navigator: NavigationCoordinator
    @Binding var pendingDeepLinkUrl: URL?
    @State private var lastProcessedUrl: URL?
    
    init(coordinator: AppCoordinator, pendingDeepLinkUrl: Binding<URL?>) {
        let restaurantProvider = RestaurantRouteProvider(
            coordinator: coordinator,
            holderRegistry: RestaurantHolderProviderImpl()
        )
        let settingsProvider = SettingsRouteProvider(
            coordinator: coordinator,
            holderRegistry: SettingsHolderProviderImpl()
        )
        _navigator = StateObject(wrappedValue: NavigationCoordinator(
            coordinator: coordinator,
            routeProviders: [restaurantProvider, settingsProvider]
        ))
        _pendingDeepLinkUrl = pendingDeepLinkUrl
    }
    
    var body: some View {
        TabNavigationView(navigator: navigator)
            .onAppear {
                handlePendingDeepLink()
            }
            .onReceive([pendingDeepLinkUrl].publisher, perform: { _ in
                if pendingDeepLinkUrl != lastProcessedUrl && pendingDeepLinkUrl != nil {
                    handlePendingDeepLink()
                }
            })
    }
    
    private func handlePendingDeepLink() {
        guard let url = pendingDeepLinkUrl else {
            print("🔗 DEBUG: handlePendingDeepLink() called but pendingDeepLinkUrl is nil")
            return
        }
        
        print("🔗 DEBUG: handlePendingDeepLink() processing: \(url)")
        lastProcessedUrl = url
        
        Task {
            print("🔗 DEBUG: Waiting 50ms to ensure listener is fully subscribed...")
            try? await Task.sleep(nanoseconds: 50_000_000)
            
            print("🔗 DEBUG: Sleep complete, calling navigator.processPendingDeepLink()")
            navigator.processPendingDeepLink(url)
            
            print("🔗 DEBUG: Clearing pendingDeepLinkUrl")
            pendingDeepLinkUrl = nil
        }
    }
}
