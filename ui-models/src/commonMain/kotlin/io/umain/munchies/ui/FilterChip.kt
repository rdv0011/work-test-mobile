package io.umain.munchies.ui

import io.umain.munchies.designtokens.DesignTokens

/**
 * FilterChip Component - KMP Common Definition
 * 
 * Represents a selectable filter chip with:
 * - Remote icon URL (48x48px)
 * - Label text (title2 style)
 * - Selection state with visual feedback
 * - Gap between icon and text (8px)
 * 
 * Design: 144x48px rounded chip (24px radius)
 * Unselected: semi-transparent white bg (#FFFFFF66), dark text
 * Selected: accent color bg (#E2A364), white text
 */

data class FilterChipData(
    val id: String,
    val label: String,
    val iconUrl: String,
    val isSelected: Boolean = false,
    val contentDescription: String = "Filter: $label"
)

data class FilterChipDimensions(
    val width: Int = DesignTokens.Sizes.Filter.width,
    val height: Int = DesignTokens.Sizes.Filter.height,
    val iconSize: Int = DesignTokens.Sizes.Filter.iconSize,
    val cornerRadius: Int = DesignTokens.BorderRadius.full,
    val iconTextGap: Int = DesignTokens.Spacing.sm,
    val shadowElevation: String = DesignTokens.Elevation.filter,
    val horizontalPadding: Int = DesignTokens.Spacing.lg
)

data class FilterChipColors(
    val unselectedBackground: String = DesignTokens.Colors.Background.filterDefault,
    val selectedBackground: String = DesignTokens.Colors.Accent.selected,
    val unselectedTextColor: String = DesignTokens.Colors.Text.picto,
    val selectedTextColor: String = DesignTokens.Colors.Text.light,
    val shadowColor: String = "#0000000A"
)

data class FilterChipTypography(
    val labelStyle: io.umain.munchies.designtokens.DesignTokens.Typography.TextStyle = 
        DesignTokens.Typography.TextStyles.title2
)
