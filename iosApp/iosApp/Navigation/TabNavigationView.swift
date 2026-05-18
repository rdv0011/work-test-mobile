//
//  TabNavigationView.swift
//  iosApp
//

import SwiftUI
import shared
import os.log

private let logger = Logger(subsystem: "com.munchies.ios", category: "TabNavigationView")

struct TabNavigationView: View {
    @ObservedObject var navigator: NavigationCoordinator
    
    @State private var restaurantListHolder: RestaurantListViewModelHolder?
    @State private var settingsHolder: SettingsViewModelHolder?
    
    var body: some View {
        ZStack {
            TabView(selection: $navigator.activeTabId) {
                restaurantTab
                    .tabItem {
                        Label(stringResource(key: "tab_restaurants"), systemImage: "house.fill")
                    }
                    .tag("restaurants")
                    .onAppear {
                        logger.debug("restaurantTab appeared, initializing holder if needed")
                        if restaurantListHolder == nil {
                            restaurantListHolder = navigator.restaurantListHolder()
                            logger.debug("restaurantListHolder initialized and cached")
                        }
                    }
                
                settingsTab
                    .tabItem {
                        Label(stringResource(key: "tab_settings"), systemImage: "gear")
                    }
                    .tag("settings")
                    .onAppear {
                        logger.debug("settingsTab appeared, initializing holder if needed")
                        if settingsHolder == nil {
                            settingsHolder = navigator.settingsViewModelHolder()
                            logger.debug("settingsHolder initialized and cached")
                        }
                    }
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
        .onAppear {
            navigator.startObservingNavigationState(lifecycleOwner: nil)
        }
    }
    
    @ViewBuilder
    private var restaurantTab: some View {
        if let holder = restaurantListHolder {
            NavigationStack(path: Binding(
                get: { navigator.tabStacks["restaurants"] ?? NavigationPath() },
                set: { navigator.tabStacks["restaurants"] = $0 }
            )) {
                RestaurantListView(
                    navigationViewModel: holder.navigationViewModel,
                    viewModel: holder.viewModel
                )
                .navigationDestination(for: Route.self) { route in
                    destinationView(for: route)
                }
            }
        } else {
            ProgressView()
        }
    }
    
    @ViewBuilder
    private var settingsTab: some View {
        if let holder = settingsHolder {
            NavigationStack(path: Binding(
                get: { navigator.tabStacks["settings"] ?? NavigationPath() },
                set: { navigator.tabStacks["settings"] = $0 }
            )) {
                SettingsView(viewModel: holder.viewModel)
                    .navigationDestination(for: Route.self) { route in
                        destinationView(for: route)
                    }
            }
        } else {
            ProgressView()
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
       
       private func getViewModelForModal(_ modal: CoreModalRoute, navigator: NavigationCoordinator) -> RestaurantDetailViewModel? {
           if let submitReview = modal as? SubmitReviewModalRoute {
               let holder = navigator.restaurantDetailHolder(restaurantId: submitReview.restaurantId)
               return holder.viewModel
           }
           return nil
       }
}
