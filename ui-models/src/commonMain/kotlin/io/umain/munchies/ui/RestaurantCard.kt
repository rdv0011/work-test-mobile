package io.umain.munchies.ui

import io.umain.munchies.designtokens.DesignTokens

/**
 * RestaurantCard Component - KMP Common Definition
 * 
 * Represents a restaurant card displaying:
 * - Restaurant image (top section)
 * - Restaurant name (title1 style)
 * - Tags/categories (subtitle1 style)
 * - Delivery time and distance (footer1 style)
 * - Star rating
 * 
 * Design: 343x196px with white background and shadow
 * Image section: 343x132px with rounded top corners
 * Info section: 64px height below image
 */

data class RestaurantCardData(
    val restaurantName: String,
    val tags: List<String>,
    val deliveryTime: String,
    val distance: String,
    val rating: Double,
    val imageUrl: String,
    val contentDescription: String = "Restaurant: $restaurantName"
)

data class RestaurantCardDimensions(
    val width: Int = DesignTokens.Sizes.Card.Restaurant.width,
    val height: Int = DesignTokens.Sizes.Card.Restaurant.height,
    val imageHeight: Int = DesignTokens.Sizes.Card.Restaurant.imageHeight,
    val cornerRadius: Int = DesignTokens.BorderRadius.md,
    val shadowElevation: String = DesignTokens.Elevation.card,
    val spacing: Int = DesignTokens.Spacing.sm,
    val tagGap: Int = DesignTokens.Spacing.xxs
)

data class RestaurantCardColors(
    val backgroundColor: String = DesignTokens.Colors.Background.card,
    val titleColor: String = DesignTokens.Colors.Text.dark,
    val tagColor: String = DesignTokens.Colors.Text.subtitle,
    val metaColor: String = DesignTokens.Colors.Text.footer,
    val starColor: String = DesignTokens.Colors.Accent.star,
    val ratingColor: String = DesignTokens.Colors.Text.footer
)

data class RestaurantCardTypography(
    val titleStyle: io.umain.munchies.designtokens.DesignTokens.Typography.TextStyle = 
        DesignTokens.Typography.TextStyles.title1,
    val tagStyle: io.umain.munchies.designtokens.DesignTokens.Typography.TextStyle = 
        DesignTokens.Typography.TextStyles.subtitle1,
    val metaStyle: io.umain.munchies.designtokens.DesignTokens.Typography.TextStyle = 
        DesignTokens.Typography.TextStyles.footer1
)
