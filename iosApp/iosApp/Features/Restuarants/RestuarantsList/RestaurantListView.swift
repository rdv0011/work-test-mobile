//
//  RestaurantListView.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-01-31.
//
import SwiftUI
import shared

struct RestaurantListView: View {
    let coordinator: AppCoordinator
    @StateObject private var holder = RestaurantListViewModelHolder(viewModel: io.umain.munchies.feature.restaurant.di.getRestaurantListViewModelIos())
    @State private var uiState: RestaurantListUiState? = nil

    private var filteredRestaurants: [RestaurantCardData] {
        // Convert shared domain restaurants to UI data
        guard let sharedState = uiState as? RestaurantListUiStateSuccess else {
            return []
        }
        return sharedState.restaurants.map { r in
            RestaurantCardData(
                restaurantName: r.name,
                tags: r.filterIds.map { $0 },
                deliveryTime: "",
                distance: "",
                rating: Double(r.rating),
                imageUrl: r.imageUrl,
                contentDescription: "Restaurant: \(r.name)"
            )
        }
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Header
            VStack(alignment: .leading, spacing: .spacingUI.sm) {
                Text(tr(.appTitle))
                    .font(.headline1)
                    .foregroundColor(.text.dark)
                
                Text(tr(.restaurantListTitle))
                    .font(.title1)
                    .foregroundColor(.text.dark)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, .spacingUI.lg)
            .padding(.vertical, .spacingUI.lg)
            
            // (Filters removed) Filter chips will be provided from shared uiState in a follow-up change.
            
            // Restaurant list
            if filteredRestaurants.isEmpty {
                VStack {
                    Text("No restaurants found")
                        .font(.title2)
                        .foregroundColor(.text.subtitle)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
            } else {
                ScrollView {
                    VStack(spacing: .spacingUI.lg) {
                        ForEach(filteredRestaurants, id: \.restaurantName) { restaurant in
                            RestaurantCardView(data: restaurant) {
                                logInfo(tag: "RestaurantList", message: "Tapped restaurant: \(restaurant.restaurantName)")
                                coordinator.navigateToRestaurantDetail(restaurantId: restaurant.restaurantName)
                            }
                        }
                    }
                    .padding(.horizontal, .spacingUI.lg)
                    .padding(.bottom, .spacingUI.lg)
                }
            }
        }
        .navigationTitle(tr(.restaurantListTitle))
        .onAppear {
            logInfo(tag: "RestaurantList", message: "Restaurant list view appeared")
        }
        .task {
            // Collect the Kotlin Flow/StateFlow exposed by the shared ViewModel and assign to Swift state
            for await state in holder.viewModel.uiState {
                self.uiState = state
            }
        }
    }
}
