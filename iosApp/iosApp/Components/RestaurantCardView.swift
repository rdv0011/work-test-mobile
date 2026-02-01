//
//  RestaurantCardView.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-02-01.
//
import SwiftUI
import shared

/// RestaurantCardView - SwiftUI wrapper for RestaurantCardData
///
/// Displays a restaurant card with:
/// - AsyncImage loaded from remote URL (132pt height, rounded top corners)
/// - Restaurant name (title1 style)
/// - Tags/categories (subtitle1 style)
/// - Delivery time and distance (footer1 style)
/// - Star rating (footer1 style)
///
/// Card dimensions: 343x196pt with shadow and white background
/// All styling driven by DesignTokens from shared KMP
struct RestaurantCardView: View {
    let data: RestaurantCardData
    var onTap: (() -> Void)? = nil
    
    var body: some View {
        let tagGap = CGFloat(DesignTokens.Spacing.shared.xs)
        let padding = CGFloat(DesignTokens.Spacing.shared.sm)
        
        VStack(alignment: .leading, spacing: 0) {
            // Image section - top 132pt with rounded corners
            AsyncImage(url: URL(string: data.imageUrl)) { phase in
                switch phase {
                case .empty:
                    ZStack {
                        Color.gray.opacity(0.2)
                        ProgressView()
                            .tint(Color.gray)
                    }
                    .frame(height: .cardRestaurantImageHeight)
                    .accessibilityLabel("Loading restaurant image")
                    
                case .success(let image):
                    image
                        .resizable()
                        .scaledToFill()
                        .frame(height: .cardRestaurantImageHeight)
                        .clipped()
                        .accessibilityLabel("Restaurant image for \(data.restaurantName)")
                    
                case .failure:
                    ZStack {
                        Color.gray.opacity(0.2)
                        Image(systemName: "photo.fill")
                            .font(.system(size: 24))
                            .foregroundColor(.gray)
                    }
                    .frame(height: .cardRestaurantImageHeight)
                    .accessibilityLabel("Failed to load restaurant image")
                    
                @unknown default:
                    EmptyView()
                }
            }
            
            // Info section - bottom 64pt
            VStack(alignment: .leading, spacing: 4) {
                // Restaurant name (title1)
                Text(data.restaurantName)
                    .font(.title1)
                    .foregroundColor(.text.dark)
                    .lineLimit(1)
                    .accessibilityLabel("Restaurant name: \(data.restaurantName)")
                
                // Tags (subtitle1) - wrapped horizontally
                if !data.tags.isEmpty {
                    HStack(spacing: tagGap) {
                        ForEach(data.tags, id: \.self) { tag in
                            Text(tag)
                                .font(.subtitle1)
                                .foregroundColor(.text.subtitle)
                                .lineLimit(1)
                        }
                    }
                    .accessibilityLabel("Tags: \(data.tags.joined(separator: ", "))")
                }
                
                // Delivery info and rating (footer1)
                HStack(spacing: 12) {
                    HStack(spacing: 4) {
                        Image(systemName: "clock.fill")
                            .font(.system(size: 8))
                            .foregroundColor(.text.footer)
                        
                        Text(data.deliveryTime)
                            .font(.footer1)
                            .foregroundColor(.text.footer)
                    }
                    
                    HStack(spacing: 4) {
                        Image(systemName: "location.fill")
                            .font(.system(size: 8))
                            .foregroundColor(.text.footer)
                        
                        Text(data.distance)
                            .font(.footer1)
                            .foregroundColor(.text.footer)
                    }
                    
                    Spacer()
                    
                    // Star rating
                    HStack(spacing: 2) {
                        Image(systemName: "star.fill")
                            .font(.system(size: 10))
                            .foregroundColor(.accent.star)
                        
                        Text(String(format: "%.1f", data.rating))
                            .font(.footer1)
                            .foregroundColor(.text.footer)
                    }
                }
                .accessibilityLabel("Delivery: \(data.deliveryTime), Distance: \(data.distance), Rating: \(String(format: "%.1f", data.rating))")
            }
            .padding(padding)
            
            Spacer(minLength: 0)
        }
        .frame(width: .cardRestaurantWidth, height: .cardRestaurantHeight)
        .background(Color.background.card)
        .cornerRadius(.borderRadiusUI.md, corners: [.topLeft, .topRight])
        .shadow(color: Color.black.opacity(0.1), radius: 2, x: 0, y: 2)
        .contentShape(Rectangle())
        .onTapGesture {
            onTap?()
        }
        .accessibilityElement(children: .combine)
        .accessibilityLabel(data.contentDescription)
    }
}

// FlowLayout that works with content
struct WrappingHStack: View {
    let spacing: CGFloat
    @ViewBuilder let content: () -> [AnyView]
    
    var body: some View {
        var width: CGFloat = 0
        var height: CGFloat = 0
        
        return ZStack(alignment: .topLeading) {
            ForEach(Array(content().enumerated()), id: \.offset) { index, item in
                item
                    .alignmentGuide(.leading) { d in
                        if abs(width - d.width) > 300 {
                            width = 0
                            height -= d.height + spacing
                        }
                        let result = width
                        width -= d.width + spacing
                        return result
                    }
                    .alignmentGuide(.top) { _ in
                        let result = height
                        return result
                    }
            }
        }
        .frame(height: height)
    }
}

// MARK: - Corner Radius Extension
extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorner(radius: radius, corners: corners))
    }
}

struct RoundedCorner: Shape {
    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners
    
    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(
            roundedRect: rect,
            byRoundingCorners: corners,
            cornerRadii: CGSize(width: radius, height: radius)
        )
        return Path(path.cgPath)
    }
}

// MARK: - Preview
#Preview {
    RestaurantCardView(
        data: RestaurantCardData(
            restaurantName: "Pizza Palace",
            tags: ["Italian", "Pizza", "Vegetarian"],
            deliveryTime: "25-35 min",
            distance: "2.5 km",
            rating: 4.5,
            imageUrl: "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=343&h=132&fit=crop",
            contentDescription: "Restaurant: Pizza Palace"
        )
    )
}
