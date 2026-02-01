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
        let tokensTypographyFontFamilies = DesignTokens.TypographyFontFamilies.shared
        let tokensTypographyFontSizes = DesignTokens.TypographyFontSizes.shared
        let tokensColorsBackground = DesignTokens.ColorsBackground.shared
        let tokensColorsText = DesignTokens.ColorsText.shared
        let tokensSpacing = DesignTokens.Spacing.shared
        let tokensBorderRadius = DesignTokens.BorderRadius.shared
        let tokensSizesCardDetail = DesignTokens.SizesCardDetail.shared
        let tokensElevation = DesignTokens.Elevation.shared
        
        VStack(alignment: .leading, spacing: 0) {
            // Title - headline1 (24pt Helvetica)
            Text(data.title)
                .font(Font.custom(tokensTypographyFontFamilies.helvetica, size: CGFloat(tokensTypographyFontSizes.headline1)))
                .foregroundColor(Color(hex: tokensColorsText.dark))
                .lineLimit(1)
                .accessibilityLabel("Title: \(data.title)")
                .padding(.top, CGFloat(tokensSpacing.lg))
                .padding(.horizontal, CGFloat(tokensSpacing.lg))
            
            Spacer(minLength: 8)
            
            // Subtitle - headline2 (16pt Helvetica)
            Text(data.subtitle)
                .font(Font.custom(tokensTypographyFontFamilies.helvetica, size: CGFloat(tokensTypographyFontSizes.headline2)))
                .foregroundColor(Color(hex: tokensColorsText.subtitle))
                .lineLimit(1)
                .accessibilityLabel("Subtitle: \(data.subtitle)")
                .padding(.horizontal, CGFloat(tokensSpacing.lg))
            
            Spacer(minLength: 8)
            
            // Status - title1 (18pt Helvetica) with color from data
            HStack(spacing: 8) {
                // Color indicator dot
                Circle()
                    .fill(Color(hex: data.statusColor))
                    .frame(width: 10, height: 10)
                    .accessibilityHidden(true)
                
                Text(data.statusText)
                    .font(Font.custom(tokensTypographyFontFamilies.helvetica, size: CGFloat(tokensTypographyFontSizes.title1)))
                    .foregroundColor(Color(hex: data.statusColor))
                    .lineLimit(1)
            }
            .accessibilityLabel("Status: \(data.statusText)")
            .accessibilityValue(data.contentDescription)
            .padding(.horizontal, CGFloat(tokensSpacing.lg))
            .padding(.bottom, CGFloat(tokensSpacing.lg))
            
            Spacer(minLength: 0)
        }
        .frame(width: CGFloat(tokensSizesCardDetail.width), height: CGFloat(tokensSizesCardDetail.height))
        .background(Color(hex: tokensColorsBackground.card))
        .cornerRadius(CGFloat(tokensBorderRadius.md))
        .shadow(color: Color(hex: tokensElevation.card).opacity(0.1), radius: 2, x: 0, y: 2)
        .accessibilityElement(children: .combine)
    }
}

// MARK: - Preview
#Preview {
    VStack(spacing: 16) {
        // Open status
        DetailCardView(
            data: DetailCardData(
                title: "Pizza Palace",
                subtitle: "Italian Restaurant",
                statusText: "Open",
                statusColor: "#2ECC71",
                contentDescription: "Restaurant status: Open"
            )
        )
        
        // Closed status
        DetailCardView(
            data: DetailCardData(
                title: "Sushi Spot",
                subtitle: "Japanese Restaurant",
                statusText: "Closed",
                statusColor: "#C0392B",
                contentDescription: "Restaurant status: Closed"
            )
        )
    }
    .padding()
}
