//
//  TabNavigationView.swift
//  iosApp
//

import SwiftUI
import shared

struct TabNavigationView: View {
    @ObservedObject var navigator: NavigationCoordinator
    private let R = StringResources.shared
    
    var body: some View {
        ZStack {
            TabView(selection: $navigator.activeTabId) {
                restaurantTab
                    .tabItem {
                        Label(stringResource(key: R.tab_restaurants), systemImage: "house.fill")
                    }
                    .tag("restaurants")
                
                settingsTab
                    .tabItem {
                        Label(stringResource(key: R.tab_settings), systemImage: "gear")
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
                            },
                            viewModel: getViewModelForModal(topModal, navigator: navigator)
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
      
      private func getViewModelForModal(_ modal: shared.ModalRoute, navigator: NavigationCoordinator) -> RestaurantDetailViewModel? {
          if let submitReview = modal as? SubmitReviewModalRoute {
              let holder = navigator.restaurantDetailHolder(restaurantId: submitReview.restaurantId)
              return holder.viewModel
          }
          return nil
      }
}
