//
//  RestaurantListView.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-01-31.
//
import SwiftUI
import shared

// Example restaurant data for testing
private let exampleRestaurants: [RestaurantCardData] = [
    RestaurantCardData(
        restaurantName: "Burger Palace",
        tags: ["Burgers", "Fast Food", "American"],
        deliveryTime: "25-35 min",
        distance: "2.4 km",
        rating: 4.8,
        imageUrl: "https://via.placeholder.com/343x132?text=Burger+Palace",
        contentDescription: "Restaurant: Burger Palace"
    ),
    RestaurantCardData(
        restaurantName: "Sushi Paradise",
        tags: ["Japanese", "Sushi", "Seafood"],
        deliveryTime: "30-45 min",
        distance: "3.1 km",
        rating: 4.6,
        imageUrl: "https://via.placeholder.com/343x132?text=Sushi+Paradise",
        contentDescription: "Restaurant: Sushi Paradise"
    ),
    RestaurantCardData(
        restaurantName: "Pizza Pizzeria",
        tags: ["Italian", "Pizza", "Pasta"],
        deliveryTime: "20-30 min",
        distance: "1.8 km",
        rating: 4.7,
        imageUrl: "https://via.placeholder.com/343x132?text=Pizza+Pizzeria",
        contentDescription: "Restaurant: Pizza Pizzeria"
    )
]

// Example filter data for testing
private let exampleFilters: [FilterChipData] = [
    FilterChipData(id: "1", label: "All", iconUrl: "https://via.placeholder.com/48x48?text=All", isSelected: true, contentDescription: "All restaurants"),
    FilterChipData(id: "2", label: "Fast Food", iconUrl: "https://via.placeholder.com/48x48?text=Fast", isSelected: false, contentDescription: "Fast Food restaurants"),
    FilterChipData(id: "3", label: "Asian", iconUrl: "https://via.placeholder.com/48x48?text=Asian", isSelected: false, contentDescription: "Asian cuisine restaurants")
]

struct RestaurantListView: View {
    let coordinator: AppCoordinator
    @State private var selectedFilterLabels: Set<String> = ["All"]
    
    private var filteredRestaurants: [RestaurantCardData] {
        if selectedFilterLabels.contains("All") {
            return exampleRestaurants
        } else {
            return exampleRestaurants.filter { restaurant in
                restaurant.tags.contains { tag in
                    selectedFilterLabels.contains { selectedLabel in
                        tag.localizedCaseInsensitiveContains(selectedLabel) ||
                        selectedLabel.localizedCaseInsensitiveContains(tag)
                    }
                }
            }
        }
    }
    
    var body: some View {
        let tokensTypographyFontFamilies = DesignTokens.TypographyFontFamilies.shared
        let tokensTypographyFontSizes = DesignTokens.TypographyFontSizes.shared
        let tokensColorsText = DesignTokens.ColorsText.shared
        let tokensSpacing = DesignTokens.Spacing.shared
        
        VStack(spacing: 0) {
            // Header
            VStack(alignment: .leading, spacing: CGFloat(tokensSpacing.sm)) {
                Text(tr(TextId.AppTitle()))
                    .font(Font.custom(tokensTypographyFontFamilies.helvetica, size: CGFloat(tokensTypographyFontSizes.headline1)))
                    .foregroundColor(Color(hex: tokensColorsText.dark))
                
                Text(tr(TextId.RestaurantListTitle()))
                    .font(Font.custom(tokensTypographyFontFamilies.helvetica, size: CGFloat(tokensTypographyFontSizes.title1)))
                    .foregroundColor(Color(hex: tokensColorsText.dark))
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, CGFloat(tokensSpacing.lg))
            .padding(.vertical, CGFloat(tokensSpacing.lg))
            
            // Filter chips
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: CGFloat(tokensSpacing.md)) {
                    ForEach(exampleFilters, id: \.self) { filter in
                        let isSelected = selectedFilterLabels.contains(filter.label)
                        FilterChipView(
                            data: filter.doCopy(id: filter.id, label: filter.label, iconUrl: filter.iconUrl, isSelected: isSelected, contentDescription: filter.contentDescription),
                            onSelectionChanged: { selected in
                                var updated = selectedFilterLabels
                                if selected {
                                    updated.remove("All")
                                    updated.insert(filter.label)
                                } else {
                                    updated.remove(filter.label)
                                    if updated.isEmpty {
                                        updated.insert("All")
                                    }
                                }
                                selectedFilterLabels = updated
                                logInfo(tag: "RestaurantList", message: "Filter selected: \(filter.label), selected: \(selected)")
                            }
                        )
                    }
                }
                .padding(.horizontal, CGFloat(tokensSpacing.lg))
            }
            .padding(.bottom, CGFloat(tokensSpacing.lg))
            
            // Restaurant list
            if filteredRestaurants.isEmpty {
                VStack {
                    Text("No restaurants found")
                        .font(Font.custom(tokensTypographyFontFamilies.helvetica, size: CGFloat(tokensTypographyFontSizes.title2)))
                        .foregroundColor(Color(hex: tokensColorsText.subtitle))
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
            } else {
                ScrollView {
                    VStack(spacing: CGFloat(tokensSpacing.lg)) {
                        ForEach(filteredRestaurants, id: \.restaurantName) { restaurant in
                            RestaurantCardView(data: restaurant) {
                                logInfo(tag: "RestaurantList", message: "Tapped restaurant: \(restaurant.restaurantName)")
                                coordinator.navigateToRestaurantDetail(restaurantId: restaurant.restaurantName)
                            }
                        }
                    }
                    .padding(.horizontal, CGFloat(tokensSpacing.lg))
                    .padding(.bottom, CGFloat(tokensSpacing.lg))
                }
            }
        }
        .navigationTitle(tr(TextId.RestaurantListTitle()))
        .onAppear {
            logInfo(tag: "RestaurantList", message: "Restaurant list view appeared")
        }
    }
}
