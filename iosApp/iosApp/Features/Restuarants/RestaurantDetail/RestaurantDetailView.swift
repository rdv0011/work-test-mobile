//
//  RestaurantDetailView.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-01-31.
//
import SwiftUI
import shared

struct RestaurantDetailView: View {
    let restaurantId: String
    let viewModel: RestaurantDetailViewModel
    
    @State private var uiState: RestaurantDetailUiState = RestaurantDetailUiState.Loading()

    var body: some View {
        ZStack {
            contentView()
            
            if isLoading() {
                loadingOverlay()
            }
        }
        .navigationTitle(tr(.restaurantDetailTitle))
        .navigationBarBackButtonHidden(false)
        .onAppear {
            logInfo(tag: "RestaurantDetail", message: "Viewing restaurant detail: \(restaurantId)")
        }
        .task(id: viewModel) {
            await observe()
        }
    }
    
    // MARK: - Content
    
    @ViewBuilder
    private func contentView() -> some View {
        VStack(spacing: .zero) {
            restaurantImage()
            ScrollView {
                detailCard()
                    .padding(.all, .spacingUI.lg)
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
        DetailCardView(
            data: DetailCardData(
                title: restaurantName(),
                tags: [],
                statusText: statusText(),
                statusColor: statusColor(),
            )
        )
    }
    
    // MARK: - Loading Overlay
    
    @ViewBuilder
    private func loadingOverlay() -> some View {
        ProgressView()
            .progressViewStyle(CircularProgressViewStyle())
            .scaleEffect(1.5)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(Color.black.opacity(0.2))
    }
    
    // MARK: - State Helpers
    
    private func isLoading() -> Bool {
        uiState is RestaurantDetailUiState.Loading
    }
    
    private func restaurantName() -> String {
        if let success = uiState as? RestaurantDetailUiState.Success {
            return success.restaurant.name
        }
        return ""
    }
    
    private func restaurantImageUrl() -> String {
        if let success = uiState as? RestaurantDetailUiState.Success {
            return success.restaurant.imageUrl
        }
        return ""
    }
    
    private func statusText() -> String {
        if let success = uiState as? RestaurantDetailUiState.Success {
            return success.status == RestaurantStatus.open ? tr(.restaurantStatusOpen) : tr(.restaurantStatusClosed)
        }
        return ""
    }
    
    private func statusColor() -> String {
        if let success = uiState as? RestaurantDetailUiState.Success {
            let tokensColorsAccent = DesignTokens.ColorsAccent.shared
            return success.status == RestaurantStatus.open ? tokensColorsAccent.positive : tokensColorsAccent.negative
        }
        return ""
    }
    
    // MARK: - Observe StateFlow
    
    @MainActor
    private func observe() async {
        for await state in asyncStateStream(viewModel) as AsyncStream<RestaurantDetailUiState> {
            uiState = state
        }
    }
}
