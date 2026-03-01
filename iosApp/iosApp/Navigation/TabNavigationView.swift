//
//  TabNavigationView.swift
//  iosApp
//

import SwiftUI
import shared

struct TabNavigationView: View {
    @ObservedObject var navigator: NavigationCoordinator
    
    var body: some View {
        ZStack {
            TabView(selection: $navigator.activeTabId) {
                restaurantTab
                    .tabItem {
                        Label(tr(.restaurants), systemImage: "house.fill")
                    }
                    .tag("restaurants")
                
                settingsTab
                    .tabItem {
                        Label(tr(.settings), systemImage: "gear")
                    }
                    .tag("settings")
            }
            
            if let topModal = navigator.showingModal {
                ZStack {
                    Color.black.opacity(0.3)
                        .ignoresSafeArea()
                        .onTapGesture {
                            if topModal.dismissOnBackgroundTap {
                                navigator.coordinator.dismissModal()
                            }
                        }
                    
                    VStack {
                        Spacer()
                        ModalDestinationView(
                            modal: topModal,
                            onDismiss: {
                                navigator.coordinator.dismissModal()
                            }
                        )
                        Spacer()
                    }
                }
                .transition(.opacity.combined(with: .scale))
            }
        }
        .animation(.easeInOut(duration: 0.2), value: navigator.showingModalKey)
    }
    
     @ViewBuilder
     private var restaurantTab: some View {
         NavigationStack(path: Binding(
             get: { navigator.tabStacks["restaurants"] ?? NavigationPath() },
             set: { navigator.tabStacks["restaurants"] = $0 }
         )) {
             let restaurantListHolder = navigator.restaurantListHolder()
             RestaurantListView(
                 navigationViewModel: restaurantListHolder.navigationViewModel,
                 viewModel: restaurantListHolder.viewModel
             )
             .navigationDestination(for: Route.self) { route in
                 destinationView(for: route)
             }
         }
     }
    
     @ViewBuilder
     private var settingsTab: some View {
         NavigationStack(path: Binding(
             get: { navigator.tabStacks["settings"] ?? NavigationPath() },
             set: { navigator.tabStacks["settings"] = $0 }
         )) {
             let settingsHolder = navigator.settingsHolder()
             SettingsView(
                 navigationViewModel: settingsHolder.navigationViewModel,
                 viewModel: settingsHolder.viewModel
             )
                 .navigationDestination(for: Route.self) { route in
                     destinationView(for: route)
                 }
         }
     }
    
     @ViewBuilder
     private func destinationView(for route: Route) -> some View {
         switch route {
         case .restaurantDetail(let restaurantId):
             let holder = navigator.restaurantDetailHolder(restaurantId: restaurantId)
             RestaurantDetailView(
                 restaurantId: restaurantId,
                 navigationViewModel: holder.navigationViewModel,
                 viewModel: holder.viewModel,
                 holder: holder
             )
         case .restaurantList:
             EmptyView()
         case .settings:
             EmptyView()
         }
     }
}
