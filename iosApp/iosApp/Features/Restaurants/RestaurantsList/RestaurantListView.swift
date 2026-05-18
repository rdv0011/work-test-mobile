//
//  RestaurantListView.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-01-31.
//
import SwiftUI
import shared
import os.log

typealias RestaurantListUiState = Feature_restaurantRestaurantListUiState

private let logger = Logger(subsystem: "com.munchies.ios", category: "RestaurantListView")

struct RestaurantListView: View {
    let navigationViewModel: RestaurantNavigationViewModel
    let viewModel: RestaurantListViewModel
    
    @State private var uiState: RestaurantListUiState = IosAggregatorExportsKt.RestaurantListUiStateLoading()
    @State private var observationTask: Task<Void, Never>?

    private var filteredRestaurants: [Feature_restaurantRestaurantCardData] {
          guard let success = IosAggregatorExportsKt.getRestaurantListUiStateAsSuccess(state: uiState) else {
              return []
          }
          return success.restaurants
      }
    
    var body: some View {
        VStack(spacing: .zero) {
            if IosAggregatorExportsKt.isRestaurantListUiStateLoading(state: uiState) {
                ProgressView()
            } else if IosAggregatorExportsKt.isRestaurantListUiStateError(state: uiState) {
                VStack {
                    Text("No restaurants found")
                        .font(.title2)
                        .foregroundColor(.text.subtitle)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
            } else if !filteredRestaurants.isEmpty {
                ScrollView {
                    VStack(spacing: .spacingUI.lg) {
                        ForEach(filteredRestaurants, id: \.id) { restaurant in
                            RestaurantCardView(data: restaurant) {
                                navigationViewModel.showRestaurantDetail(restaurantId: restaurant.id)
                            }
                        }
                    }
                    .padding(.horizontal, .spacingUI.lg)
                    .padding(.bottom, .spacingUI.lg)
                }
            }
        }
        .navigationTitle(stringResource(key: "restaurant_list_title"))
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    if let success = IosAggregatorExportsKt.getRestaurantListUiStateAsSuccess(state: uiState) {
                        let selectedFilterIds = success.filters.map { $0.id }
                        navigationViewModel.showFilterModal(preSelectedFilters: selectedFilterIds as [String])
                    }
                }) {
                    Text("Filters")
                }
            }
        }
        .task(id: viewModel) {
            let task = Task {
                await observe()
            }
            self.observationTask = task
        }
        .onDisappear {
            logger.debug("RestaurantListView: onDisappear - cancelling observation task")
            observationTask?.cancel()
            observationTask = nil
        }
    }
    
    @MainActor
    private func observe() async {
        logger.debug("observe: Starting to observe StateFlow")
        var count = 0
        let stream: AsyncStream<RestaurantListUiState> = asyncStateStream(viewModel)
        for await state in stream {
            count += 1
            logger.debug("observe: ✓ Received state update #\(count)")
            logger.debug("observe: state type: \(String(describing: type(of: state)))")
            logger.debug("observe: uiState before update: \(String(describing: self.uiState))")
            self.uiState = state
            logger.debug("observe: uiState after update: \(String(describing: self.uiState))")
        }
        logger.debug("observe: AsyncStream completed (unsubscribed)")
    }
}
