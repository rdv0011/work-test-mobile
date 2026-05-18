//
//  RestaurantDetailView.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-01-31.
//
import SwiftUI
import shared
import os.log

typealias RestaurantDetailUiState = Feature_restaurantRestaurantDetailUiState
typealias RestaurantStatus = Feature_restaurantRestaurantStatus

private let logger = Logger(subsystem: "com.munchies.ios", category: "RestaurantDetailView")

struct RestaurantDetailView: View {
    let restaurantId: String
    let navigationViewModel: RestaurantNavigationViewModel
    let viewModel: RestaurantDetailViewModel
    let holder: RestaurantDetailViewModelHolder
    
    @State private var uiState: RestaurantDetailUiState = IosAggregatorExportsKt.RestaurantDetailUiStateLoading()
    @State private var observationTask: Task<Void, Never>?
    
    var body: some View {
        ZStack {
            contentView()
            
            if isLoading() {
                loadingOverlay()
            }
        }
        .navigationTitle(stringResource(key: "restaurant_detail_title"))
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .navigationBarLeading) {
                Button(action: {
                    navigationViewModel.navigateBack()
                }) {
                    HStack(spacing: 4) {
                        Image(systemName: "chevron.left")
                        Text(stringResource(key: "accessibility_back_button"))
                    }
                }
            }
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(action: {
                    navigationViewModel.showSubmitReviewModal(restaurantId: restaurantId)
                }) {
                    Text("Leave a Review")
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
            logger.debug("RestaurantDetailView: onDisappear - cancelling observation task")
            observationTask?.cancel()
            observationTask = nil
        }
    }
    
    @ViewBuilder
    private func contentView() -> some View {
        ZStack(alignment: .top) {
            // Background scroll content
            ScrollView {
                VStack(spacing: 0) {
                    Color.clear
                        .frame(height: 200)
                    
                    Spacer(minLength: .spacingUI.xl)
                }
            }
            
            // Image + Card overlay
            VStack(spacing: 0) {
                restaurantImage()
                    .frame(height: 200)
                
                detailCard()
                    .padding(.horizontal, 16)
                    .padding(.top, -45)
                
                Spacer()
            }
        }
    }
    
    @ViewBuilder
    private func restaurantImage() -> some View {
        AsyncImage(url: URL(string: restaurantImageUrl())) { phase in
            switch phase {
            case .empty:
                Color.gray.opacity(0.2)
                    .frame(height: 200)
                    .frame(maxWidth: .infinity)
            case .success(let image):
                image
                    .resizable()
                    .scaledToFill()
                    .frame(height: 200)
                    .frame(maxWidth: .infinity)
                    .clipped()
            case .failure:
                Image(systemName: "photo")
                    .frame(height: 200)
                    .frame(maxWidth: .infinity)
                    .background(Color.gray.opacity(0.2))
            @unknown default:
                EmptyView()
            }
        }
    }
    
    @ViewBuilder
    private func detailCard() -> some View {
        if let success = IosAggregatorExportsKt.getRestaurantDetailUiStateAsSuccess(state: uiState) {
            VStack(alignment: .leading, spacing: 12) {
                Text(success.detailCardData.title)
                    .font(.system(.title2, design: .default))
                    .foregroundColor(.text.dark)
                
                if !success.detailCardData.tags.isEmpty {
                    Text(success.detailCardData.tags.joined(separator: " • "))
                        .font(.system(.caption, design: .default))
                        .foregroundColor(.text.subtitle)
                        .lineLimit(1)
                }
                
                Text(success.detailCardData.statusText)
                    .font(.system(size: 18, weight: .regular, design: .default))
                    .foregroundColor(Color(hex: success.detailCardData.statusColor))
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(16)
            .background(Color.white)
            .cornerRadius(12)
            .shadow(color: Color.black.opacity(0.1), radius: 4, x: 0, y: 4)
        } else {
            EmptyView()
        }
    }
    
    @ViewBuilder
    private func loadingOverlay() -> some View {
        ProgressView()
            .progressViewStyle(CircularProgressViewStyle())
            .scaleEffect(1.5)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(Color.black.opacity(0.2))
    }
    
    private func isLoading() -> Bool {
        IosAggregatorExportsKt.isRestaurantDetailUiStateLoading(state: uiState)
    }
    
    private func restaurantName() -> String {
        if let success = IosAggregatorExportsKt.getRestaurantDetailUiStateAsSuccess(state: uiState) {
            return success.detailCardData.title
        }
        return ""
    }
    
    private func restaurantImageUrl() -> String {
        if let success = IosAggregatorExportsKt.getRestaurantDetailUiStateAsSuccess(state: uiState) {
            return success.detailCardData.imageUrl ?? ""
        }
        return ""
    }
    
    private func statusText() -> String {
        if let success = IosAggregatorExportsKt.getRestaurantDetailUiStateAsSuccess(state: uiState) {
            return success.detailCardData.statusText
        }
        return ""
    }
    
    private func statusColor() -> String {
        if let success = IosAggregatorExportsKt.getRestaurantDetailUiStateAsSuccess(state: uiState) {
            return success.detailCardData.statusColor
        }
        return ""
    }
    
    @MainActor
    private func observe() async {
        logger.debug("observe: Starting to observe StateFlow")
        var count = 0
        let stream: AsyncStream<RestaurantDetailUiState> = asyncStateStream(viewModel)
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
