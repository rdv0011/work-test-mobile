//
//  RestaurantDetailView.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-01-31.
//
import SwiftUI
import shared

// Example restaurant details for testing
private let restaurantDetails: [String: (name: String, description: String, imageUrl: String, opensAt: String, closesAt: String)] = [
    "Burger Palace": (
        name: "Burger Palace",
        description: "Delicious handcrafted burgers with premium toppings",
        imageUrl: "https://via.placeholder.com/343x200?text=Burger+Palace",
        opensAt: "11:00",
        closesAt: "23:00"
    ),
    "Sushi Paradise": (
        name: "Sushi Paradise",
        description: "Fresh daily sushi and authentic Japanese cuisine",
        imageUrl: "https://via.placeholder.com/343x200?text=Sushi+Paradise",
        opensAt: "12:00",
        closesAt: "22:00"
    ),
    "Pizza Pizzeria": (
        name: "Pizza Pizzeria",
        description: "Wood-fired pizzas with authentic Italian ingredients",
        imageUrl: "https://via.placeholder.com/343x200?text=Pizza+Pizzeria",
        opensAt: "10:00",
        closesAt: "23:30"
    )
]

private func getRestaurantDetail(_ restaurantId: String) -> (name: String, description: String, imageUrl: String, opensAt: String, closesAt: String) {
    return restaurantDetails[restaurantId] ?? (
        name: restaurantId,
        description: "Restaurant details unavailable",
        imageUrl: "https://via.placeholder.com/343x200?text=Restaurant",
        opensAt: "09:00",
        closesAt: "23:00"
    )
}

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
    
    var body: some View {
        let tokensTypographyFontFamilies = DesignTokens.TypographyFontFamilies.shared
        let tokensTypographyFontSizes = DesignTokens.TypographyFontSizes.shared
        let tokesnColorsAccent = DesignTokens.ColorsAccent.shared
        let tokensSpacing = DesignTokens.Spacing.shared
        let tokensColorsText = DesignTokens.ColorsText.shared
        let details = getRestaurantDetail(restaurantId)
        let isOpen = isRestaurantOpen(opensAt: details.opensAt, closesAt: details.closesAt)
        let statusColor = isOpen ? tokesnColorsAccent.positive : tokesnColorsAccent.negative
        let statusText = isOpen ? tr(TextId.RestaurantStatusOpen.shared) : tr(TextId.RestaurantStatusClosed.shared)
        
        VStack(spacing: 0) {
            // Restaurant image
            AsyncImage(url: URL(string: details.imageUrl)) { phase in
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
                VStack(alignment: .leading, spacing: CGFloat(tokensSpacing.lg)) {
                    // Detail card
                    DetailCardView(
                        data: DetailCardData(
                            title: details.name,
                            subtitle: details.description,
                            statusText: statusText,
                            statusColor: statusColor,
                            contentDescription: ""
                        )
                    )
                    
                    // Hours of operation
                    VStack(alignment: .leading, spacing: CGFloat(tokensSpacing.sm)) {
                        Text("Hours of Operation")
                            .font(Font.custom(tokensTypographyFontFamilies.helvetica, size: CGFloat(tokensTypographyFontSizes.title1)))
                            .foregroundColor(Color(hex: tokensColorsText.dark))
                        
                        HStack {
                            Text("Opens: \(details.opensAt)")
                                .font(Font.custom(tokensTypographyFontFamilies.helvetica, size: CGFloat(tokensTypographyFontSizes.title2)))
                            Spacer()
                            Text("Closes: \(details.closesAt)")
                                .font(Font.custom(tokensTypographyFontFamilies.helvetica, size: CGFloat(tokensTypographyFontSizes.title2)))
                        }
                        .foregroundColor(Color(hex: tokensColorsText.subtitle))
                    }
                    .padding(CGFloat(tokensSpacing.lg))
                    .frame(maxWidth: .infinity, alignment: .leading)
                }
                .padding(.horizontal, CGFloat(tokensSpacing.lg))
                .padding(.vertical, CGFloat(tokensSpacing.lg))
            }
        }
        .navigationTitle(tr(TextId.RestaurantDetailTitle()))
        .navigationBarBackButtonHidden(false)
        .onAppear {
            logInfo(tag: "RestaurantDetail", message: "Viewing restaurant detail: \(restaurantId)")
        }
    }
}
