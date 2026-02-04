//
//  DetailCardView.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-02-01.
//
import SwiftUI
import shared

/// DetailCardView - SwiftUI wrapper for DetailCardData
///
/// Displays restaurant detail card with:
/// - Title at top (headline1/24pt Helvetica style)
/// - Subtitle in middle (headline2/16pt Helvetica style)
/// - Status line at bottom with color indicator (title1/18pt Helvetica style)
///
/// Dimensions: 343x144pt white card with 12pt corners
/// Status color: Green (#2ECC71) for open, Red (#C0392B) for closed
struct DetailCardView: View {
    let data: DetailCardData
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(data.title)
                .font(.headline1)
                .foregroundColor(.text.dark)
                .lineLimit(1)
                .accessibilityLabel("Title: \(data.title)")
                .padding(.top, .spacingUI.lg)
                .padding(.horizontal, .spacingUI.lg)
            
            Spacer(minLength: 8)
            
            Text(data.subtitle)
                .font(.headline2)
                .foregroundColor(.text.subtitle)
                .lineLimit(1)
                .accessibilityLabel("Subtitle: \(data.subtitle)")
                .padding(.horizontal, .spacingUI.lg)
            
            Spacer(minLength: 8)
            
            HStack(spacing: 8) {
                Circle()
                    .fill(Color(hex: data.statusColor))
                    .frame(width: 10, height: 10)
                    .accessibilityHidden(true)
                
                Text(data.statusText)
                    .font(.title1)
                    .foregroundColor(Color(hex: data.statusColor))
                    .lineLimit(1)
            }
            .accessibilityLabel("Status: \(data.statusText)")
            .accessibilityValue(data.contentDescription)
            .padding(.horizontal, .spacingUI.lg)
            .padding(.bottom, .spacingUI.lg)
            
            Spacer(minLength: 0)
        }
        .frame(width: .cardDetailWidth, height: .cardDetailHeight)
        .background(Color.background.card)
        .cornerRadius(.borderRadiusUI.md)
        .cardElevation()
        .accessibilityElement(children: .combine)
    }
}

// MARK: - Preview
#Preview {
    VStack(spacing: 16) {
        DetailCardView(
            data: DetailCardData(
                title: "Pizza Palace",
                subtitle: "Italian Restaurant",
                statusText: "Open",
                statusColor: "#2ECC71",
                contentDescription: "Famous for its wood-fired pizzas"
            )
        )
        
        DetailCardView(
            data: DetailCardData(
                title: "Sushi Spot",
                subtitle: "Japanese Restaurant",
                statusText: "Closed",
                statusColor: "#C0392B",
                contentDescription: "Known for fresh sushi and sashimi"
            )
        )
    }
    .padding()
}
