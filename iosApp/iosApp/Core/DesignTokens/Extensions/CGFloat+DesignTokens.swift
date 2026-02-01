import SwiftUI
import shared

extension CGFloat {
    static var spacingUI: CGFloatSpacingNamespace { CGFloatSpacingNamespace() }
    static var borderRadiusUI: CGFloatBorderRadiusNamespace { CGFloatBorderRadiusNamespace() }
    
    static var iconSmall: CGFloat { DesignTokens.iOS.size.iconUI.small }
    static var iconMedium: CGFloat { DesignTokens.iOS.size.iconUI.medium }
    static var iconLarge: CGFloat { DesignTokens.iOS.size.iconUI.large }
    
    static var cardRestaurantWidth: CGFloat { DesignTokens.iOS.size.cardRestaurantUI.width }
    static var cardRestaurantHeight: CGFloat { DesignTokens.iOS.size.cardRestaurantUI.height }
    static var cardRestaurantImageHeight: CGFloat { DesignTokens.iOS.size.cardRestaurantUI.imageHeight }
    
    static var cardDetailWidth: CGFloat { DesignTokens.iOS.size.cardDetailUI.width }
    static var cardDetailHeight: CGFloat { DesignTokens.iOS.size.cardDetailUI.height }
    
    static var filterWidth: CGFloat { DesignTokens.iOS.size.filterUI.width }
    static var filterHeight: CGFloat { DesignTokens.iOS.size.filterUI.height }
    static var filterIconSize: CGFloat { DesignTokens.iOS.size.filterUI.iconSize }
}

struct CGFloatSpacingNamespace {
    var none: CGFloat { DesignTokens.iOS.spacingUI.none }
    var xxs: CGFloat { DesignTokens.iOS.spacingUI.xxs }
    var xs: CGFloat { DesignTokens.iOS.spacingUI.xs }
    var sm: CGFloat { DesignTokens.iOS.spacingUI.sm }
    var md: CGFloat { DesignTokens.iOS.spacingUI.md }
    var lg: CGFloat { DesignTokens.iOS.spacingUI.lg }
    var xl: CGFloat { DesignTokens.iOS.spacingUI.xl }
}

struct CGFloatBorderRadiusNamespace {
    var none: CGFloat { DesignTokens.iOS.borderRadiusUI.none }
    var sm: CGFloat { DesignTokens.iOS.borderRadiusUI.sm }
    var md: CGFloat { DesignTokens.iOS.borderRadiusUI.md }
    var full: CGFloat { DesignTokens.iOS.borderRadiusUI.full }
}
