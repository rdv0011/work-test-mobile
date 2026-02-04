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
    let viewModel: RestaurantListViewModel
    
    @State private var uiState: RestaurantListUiState = RestaurantListUiState.Loading()

    private var filteredRestaurants: [RestaurantCardData] {
        guard let sharedState = uiState as? RestaurantListUiState.Success else {
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
        VStack(spacing: .zero) {
            if uiState is RestaurantListUiState.Loading {
                ProgressView()
            } else if uiState is RestaurantListUiState.Error {
                VStack {
                    Text("No restaurants found")
                        .font(.title2)
                        .foregroundColor(.text.subtitle)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
            } else if !filteredRestaurants.isEmpty {
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
            viewModel.load()
            logInfo(tag: "RestaurantList", message: "Restaurant list view appeared")
        }
        .task(id: viewModel) {
            for await state in asyncStateStream(viewModel) as AsyncStream<RestaurantListUiState> {
                self.uiState = state
            }
        }
    }
}
