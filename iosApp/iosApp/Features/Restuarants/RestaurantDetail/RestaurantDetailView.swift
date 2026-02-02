//
//  RestaurantDetailView.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-01-31.
//
import SwiftUI
import shared

private func isRestaurantOpen(opensAt: String, closesAt: String) -> Bool {
    let dateFormatter = DateFormatter()
    dateFormatter.dateFormat = "HH:mm"
    
    let now = dateFormatter.string(from: Date())
    
    guard let openTime = dateFormatter.date(from: opensAt),
          let closeTime = dateFormatter.date(from: closesAt),
          let currentTime = dateFormatter.date(from: now) else {
        return true
    }
    
    if closeTime < openTime {
        return currentTime >= openTime || currentTime < closeTime
    } else {
        return currentTime >= openTime && currentTime < closeTime
    }
}

struct RestaurantDetailView: View {
    let restaurantId: String
    let coordinator: AppCoordinator
    // Use holder pattern to collect uiState from shared ViewModel
    @StateObject private var holder = RestaurantDetailViewModelHolder(viewModel: FeatureRestaurantIosKt.getRestaurantDetailViewModelIos())
    @State private var uiState: RestaurantDetailUiState? = nil
    
    var body: some View {
        let tokesnColorsAccent = DesignTokens.ColorsAccent.shared

        // Prepare data from shared view model if available, otherwise fallback to minimal placeholders
        let detailsName: String
        let detailsDescription: String
        let detailsImageUrl: String
        var statusText: String = ""
        var statusColor = tokesnColorsAccent.positive

        if let restaurant = viewModel.restaurant {
            detailsName = restaurant.name
            detailsDescription = restaurant.description
            detailsImageUrl = restaurant.imageUrl
            // Map status if available
            statusText = restaurant.status == RestaurantStatus.OPEN ? tr(.restaurantStatusOpen) : tr(.restaurantStatusClosed)
            // statusColor left as positive/negative based on status
            statusColor = restaurant.status == RestaurantStatus.OPEN ? tokesnColorsAccent.positive : tokesnColorsAccent.negative
        } else {
            detailsName = restaurantId
            detailsDescription = ""
            detailsImageUrl = "https://via.placeholder.com/343x200?text=Restaurant"
            // fallback: show unknown/closed
            statusText = tr(.restaurantStatusClosed)
            statusColor = tokesnColorsAccent.negative
        }
        
        VStack(spacing: 0) {
            // Restaurant image
            AsyncImage(url: URL(string: detailsImageUrl)) { phase in
                switch phase {
                case .empty:
                    ProgressView()
                        .frame(height: 200)
                        .frame(maxWidth: .infinity)
                        .background(Color.gray.opacity(0.2))
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
            
            // Content section
            ScrollView {
                VStack(alignment: .leading, spacing: .spacingUI.lg) {
                    // Detail card
                    DetailCardView(
                        data: DetailCardData(
                            title: detailsName,
                            subtitle: detailsDescription,
                            statusText: statusText,
                            statusColor: statusColor,
                            contentDescription: ""
                        )
                    )
                    
                    // Hours of operation
                    VStack(alignment: .leading, spacing: .spacingUI.sm) {
                        Text("Hours of Operation")
                            .font(.title1)
                            .foregroundColor(.text.dark)
                        
                        HStack {
                            Text("Opens: --:--")
                                .font(.title2)
                            Spacer()
                            Text("Closes: --:--")
                                .font(.title2)
                        }
                        .foregroundColor(.text.subtitle)
                    }
                    .padding(.spacingUI.lg)
                    .frame(maxWidth: .infinity, alignment: .leading)
                }
                .padding(.horizontal, .spacingUI.lg)
                .padding(.vertical, .spacingUI.lg)
            }
        }
        .navigationTitle(tr(.restaurantDetailTitle))
        .navigationBarBackButtonHidden(false)
        .onAppear {
            logInfo(tag: "RestaurantDetail", message: "Viewing restaurant detail: \(restaurantId)")
            // trigger loading the restaurant
            holder.viewModel.loadRestaurantId(restaurantId)
        }
        .task {
            for await state in holder.viewModel.uiState {
                self.uiState = state
            }
        }
    }
}
