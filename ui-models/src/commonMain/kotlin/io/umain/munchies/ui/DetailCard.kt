package io.umain.munchies.ui

import io.umain.munchies.designtokens.DesignTokens

/**
 * DetailCard Component - KMP Common Definition
 * 
 * Represents a detail card displaying restaurant status with:
 * - Title at top (headline1 style)
 * - Subtitle in middle (headline2 style)
 * - Status line at bottom with color indicator (title1 style)
 * 
 * Design: 343x144px white card with rounded corners (12px)
 * Status colors: green (#2ECC71) for open, red (#C0392B) for closed
 * Shadow and spacing from design tokens
 */

data class DetailCardData(
    val title: String,
    val subtitle: String,
    val statusText: String,
    val statusColor: String,
    val contentDescription: String = "Restaurant status: $statusText"
)

data class DetailCardDimensions(
    val width: Int = DesignTokens.Sizes.Card.Detail.width,
    val height: Int = DesignTokens.Sizes.Card.Detail.height,
    val cornerRadius: Int = DesignTokens.BorderRadius.md,
    val shadowElevation: String = DesignTokens.Elevation.card,
    val paddingHorizontal: Int = DesignTokens.Spacing.lg,
    val paddingVertical: Int = DesignTokens.Spacing.lg,
    val titleTopOffset: Int = DesignTokens.Spacing.lg,
    val subtitleTopOffset: Int = 58,
    val statusTopOffset: Int = 93
)

data class DetailCardColors(
    val backgroundColor: String = DesignTokens.Colors.Background.card,
    val titleColor: String = DesignTokens.Colors.Text.dark,
    val subtitleColor: String = DesignTokens.Colors.Text.subtitle
)

data class DetailCardTypography(
    val titleStyle: io.umain.munchies.designtokens.DesignTokens.Typography.TextStyle = 
        DesignTokens.Typography.TextStyles.headline1,
    val subtitleStyle: io.umain.munchies.designtokens.DesignTokens.Typography.TextStyle = 
        DesignTokens.Typography.TextStyles.headline2,
    val statusStyle: io.umain.munchies.designtokens.DesignTokens.Typography.TextStyle = 
        DesignTokens.Typography.TextStyles.title1
)
