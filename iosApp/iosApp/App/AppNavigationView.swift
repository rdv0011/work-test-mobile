import SwiftUI
import shared
import Combine

private let logTag = "AppNavigationView"

struct AppNavigationView: View {
    @StateObject private var navigator: NavigationCoordinator
    @Binding var pendingDeepLinkUrl: URL?
    
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
                 processPendingDeepLinkIfAvailable()
             }
             .onReceive(Just(pendingDeepLinkUrl).compactMap { $0 }, perform: handlePendingDeepLink)
     }
     
     private func processPendingDeepLinkIfAvailable() {
         guard let url = pendingDeepLinkUrl else { return }
         navigator.coordinator.onListenerReady {
             self.navigator.processPendingDeepLink(url)
             self.pendingDeepLinkUrl = nil
         }
     }
     
      private func handlePendingDeepLink(_ url: URL) {
          logInfo(tag: logTag, message: "🔗 handlePendingDeepLink() processing: \(url)")
          navigator.coordinator.onListenerReady {
              self.navigator.processPendingDeepLink(url)
              self.pendingDeepLinkUrl = nil
          }
      }
}

