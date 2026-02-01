import SwiftUI
import shared

/**
 * iOS DesignTokens Extension
 * 
 * Unified access to all design tokens in a single scope.
 * Includes Color and CGFloat type duplicates for easier SwiftUI usage.
 * Token names and values aligned with shared KMP DesignTokens.
 * 
 * Usage:
 *   let dt = DesignTokens.iOS
 *   Text("Title")
 *       .font(.custom(dt.typography.fontFamily.helvetica, size: dt.typography.fontSizeUI.title1))
 *       .foregroundColor(dt.colorUI.text.dark)
 *       .padding(dt.spacingUI.lg)
 */

extension DesignTokens {
    static var iOS: IOSDesignTokens {
        IOSDesignTokens.shared
    }
}

class IOSDesignTokens {
    static let shared = IOSDesignTokens()
    
    private init() {}
    
    struct ColorTokens {
        struct Text {
            let dark: String
            let light: String
            let subtitle: String
            let footer: String
            let picto: String
            
            init() {
                let tokensColorsText = DesignTokens.ColorsText.shared
                self.dark = tokensColorsText.dark
                self.light = tokensColorsText.light
                self.subtitle = tokensColorsText.subtitle
                self.footer = tokensColorsText.footer
                self.picto = tokensColorsText.picto
            }
        }
        
        struct Background {
            let primary: String
            let card: String
            let filterDefault: String
            
            init() {
                let tokensColorsBackground = DesignTokens.ColorsBackground.shared
                self.primary = tokensColorsBackground.primary
                self.card = tokensColorsBackground.card
                self.filterDefault = tokensColorsBackground.filterDefault
            }
        }
        
        struct Accent {
            let selected: String
            let positive: String
            let negative: String
            let star: String
            let brightRed: String
            
            init() {
                let tokensColorsAccent = DesignTokens.ColorsAccent.shared
                self.selected = tokensColorsAccent.selected
                self.positive = tokensColorsAccent.positive
                self.negative = tokensColorsAccent.negative
                self.star = tokensColorsAccent.star
                self.brightRed = tokensColorsAccent.brightRed
            }
        }
        
        let text = Text()
        let background = Background()
        let accent = Accent()
    }
    
    struct ColorUITokens {
        struct Text {
            var dark: Color { Color(hex: DesignTokens.ColorsText.shared.dark) }
            var light: Color { Color(hex: DesignTokens.ColorsText.shared.light) }
            var subtitle: Color { Color(hex: DesignTokens.ColorsText.shared.subtitle) }
            var footer: Color { Color(hex: DesignTokens.ColorsText.shared.footer) }
            var picto: Color { Color(hex: DesignTokens.ColorsText.shared.picto) }
        }
        
        struct Background {
            var primary: Color { Color(hex: DesignTokens.ColorsBackground.shared.primary) }
            var card: Color { Color(hex: DesignTokens.ColorsBackground.shared.card) }
            var filterDefault: Color { Color(hex: DesignTokens.ColorsBackground.shared.filterDefault) }
        }
        
        struct Accent {
            var selected: Color { Color(hex: DesignTokens.ColorsAccent.shared.selected) }
            var positive: Color { Color(hex: DesignTokens.ColorsAccent.shared.positive) }
            var negative: Color { Color(hex: DesignTokens.ColorsAccent.shared.negative) }
            var star: Color { Color(hex: DesignTokens.ColorsAccent.shared.star) }
            var brightRed: Color { Color(hex: DesignTokens.ColorsAccent.shared.brightRed) }
        }
        
        let text = Text()
        let background = Background()
        let accent = Accent()
    }
    
    struct TypographyTokens {
        struct FontFamily {
            let helvetica: String
            let poppins: String
            let inter: String
            
            init() {
                let tokensTypographyFontFamilies = DesignTokens.TypographyFontFamilies.shared
                self.helvetica = tokensTypographyFontFamilies.helvetica
                self.poppins = tokensTypographyFontFamilies.poppins
                self.inter = tokensTypographyFontFamilies.inter
            }
        }
        
        struct FontSize {
            let headline1: Int32
            let title1: Int32
            let headline2: Int32
            let title2: Int32
            let subtitle1: Int32
            let footer1: Int32
            
            init() {
                let tokensTypographyFontSizes = DesignTokens.TypographyFontSizes.shared
                self.headline1 = tokensTypographyFontSizes.headline1
                self.title1 = tokensTypographyFontSizes.title1
                self.headline2 = tokensTypographyFontSizes.headline2
                self.title2 = tokensTypographyFontSizes.title2
                self.subtitle1 = tokensTypographyFontSizes.subtitle1
                self.footer1 = tokensTypographyFontSizes.footer1
            }
        }
        
        struct FontSizeUI {
            var headline1: CGFloat { CGFloat(DesignTokens.TypographyFontSizes.shared.headline1) }
            var title1: CGFloat { CGFloat(DesignTokens.TypographyFontSizes.shared.title1) }
            var headline2: CGFloat { CGFloat(DesignTokens.TypographyFontSizes.shared.headline2) }
            var title2: CGFloat { CGFloat(DesignTokens.TypographyFontSizes.shared.title2) }
            var subtitle1: CGFloat { CGFloat(DesignTokens.TypographyFontSizes.shared.subtitle1) }
            var footer1: CGFloat { CGFloat(DesignTokens.TypographyFontSizes.shared.footer1) }
        }
        
        struct FontWeight {
            let regular: Int32
            let medium: Int32
            let bold: Int32
            
            init() {
                let tokensTypographyFontWeights = DesignTokens.TypographyFontWeights.shared
                self.regular = tokensTypographyFontWeights.regular
                self.medium = tokensTypographyFontWeights.medium
                self.bold = tokensTypographyFontWeights.bold
            }
        }
        
        struct LineHeight {
            let headline1: Int32
            let title1: Int32
            let headline2: Int32
            let title2: Int32
            let subtitle1: Int32
            let footer1: Int32
            
            init() {
                let tokensTypographyLineHeights = DesignTokens.TypographyLineHeights.shared
                self.headline1 = tokensTypographyLineHeights.headline1
                self.title1 = tokensTypographyLineHeights.title1
                self.headline2 = tokensTypographyLineHeights.headline2
                self.title2 = tokensTypographyLineHeights.title2
                self.subtitle1 = tokensTypographyLineHeights.subtitle1
                self.footer1 = tokensTypographyLineHeights.footer1
            }
        }
        
        struct LineHeightUI {
            var headline1: CGFloat { CGFloat(DesignTokens.TypographyLineHeights.shared.headline1) }
            var title1: CGFloat { CGFloat(DesignTokens.TypographyLineHeights.shared.title1) }
            var headline2: CGFloat { CGFloat(DesignTokens.TypographyLineHeights.shared.headline2) }
            var title2: CGFloat { CGFloat(DesignTokens.TypographyLineHeights.shared.title2) }
            var subtitle1: CGFloat { CGFloat(DesignTokens.TypographyLineHeights.shared.subtitle1) }
            var footer1: CGFloat { CGFloat(DesignTokens.TypographyLineHeights.shared.footer1) }
        }
        
        let fontFamily = FontFamily()
        let fontSize = FontSize()
        let fontSizeUI = FontSizeUI()
        let fontWeight = FontWeight()
        let lineHeight = LineHeight()
        let lineHeightUI = LineHeightUI()
    }
    
    struct SpacingTokens {
        let none: Int32
        let xxs: Int32
        let xs: Int32
        let sm: Int32
        let md: Int32
        let lg: Int32
        let xl: Int32
        
        init() {
            let tokensSpacing = DesignTokens.Spacing.shared
            self.none = tokensSpacing.none
            self.xxs = tokensSpacing.xxs
            self.xs = tokensSpacing.xs
            self.sm = tokensSpacing.sm
            self.md = tokensSpacing.md
            self.lg = tokensSpacing.lg
            self.xl = tokensSpacing.xl
        }
    }
    
    struct SpacingUITokens {
        var none: CGFloat { CGFloat(DesignTokens.Spacing.shared.none) }
        var xxs: CGFloat { CGFloat(DesignTokens.Spacing.shared.xxs) }
        var xs: CGFloat { CGFloat(DesignTokens.Spacing.shared.xs) }
        var sm: CGFloat { CGFloat(DesignTokens.Spacing.shared.sm) }
        var md: CGFloat { CGFloat(DesignTokens.Spacing.shared.md) }
        var lg: CGFloat { CGFloat(DesignTokens.Spacing.shared.lg) }
        var xl: CGFloat { CGFloat(DesignTokens.Spacing.shared.xl) }
    }
    
    struct BorderRadiusTokens {
        let none: Float
        let sm: Float
        let md: Int32
        let full: Int32
        
        init() {
            let tokensBorderRadius = DesignTokens.BorderRadius.shared
            self.none = Float(tokensBorderRadius.none)
            self.sm = tokensBorderRadius.sm
            self.md = tokensBorderRadius.md
            self.full = tokensBorderRadius.full
        }
    }
    
    struct BorderRadiusUITokens {
        var none: CGFloat { CGFloat(DesignTokens.BorderRadius.shared.none) }
        var sm: CGFloat { CGFloat(DesignTokens.BorderRadius.shared.sm) }
        var md: CGFloat { CGFloat(DesignTokens.BorderRadius.shared.md) }
        var full: CGFloat { CGFloat(DesignTokens.BorderRadius.shared.full) }
    }
    
    struct SizeTokens {
        struct Icon {
            let small: Int32
            let medium: Int32
            let large: Int32
            
            init() {
                let tokensSizesIcon = DesignTokens.SizesIcon.shared
                self.small = tokensSizesIcon.small
                self.medium = tokensSizesIcon.medium
                self.large = tokensSizesIcon.large
            }
        }
        
        struct IconUI {
            var small: CGFloat { CGFloat(DesignTokens.SizesIcon.shared.small) }
            var medium: CGFloat { CGFloat(DesignTokens.SizesIcon.shared.medium) }
            var large: CGFloat { CGFloat(DesignTokens.SizesIcon.shared.large) }
        }
        
        struct CardRestaurant {
            let width: Int32
            let height: Int32
            let imageHeight: Int32
            
            init() {
                let tokensSizesCardRestaurant = DesignTokens.SizesCardRestaurant.shared
                self.width = tokensSizesCardRestaurant.width
                self.height = tokensSizesCardRestaurant.height
                self.imageHeight = tokensSizesCardRestaurant.imageHeight
            }
        }
        
        struct CardRestaurantUI {
            var width: CGFloat { CGFloat(DesignTokens.SizesCardRestaurant.shared.width) }
            var height: CGFloat { CGFloat(DesignTokens.SizesCardRestaurant.shared.height) }
            var imageHeight: CGFloat { CGFloat(DesignTokens.SizesCardRestaurant.shared.imageHeight) }
        }
        
        struct CardDetail {
            let width: Int32
            let height: Int32
            
            init() {
                let tokensSizesCardDetail = DesignTokens.SizesCardDetail.shared
                self.width = tokensSizesCardDetail.width
                self.height = tokensSizesCardDetail.height
            }
        }
        
        struct CardDetailUI {
            var width: CGFloat { CGFloat(DesignTokens.SizesCardDetail.shared.width) }
            var height: CGFloat { CGFloat(DesignTokens.SizesCardDetail.shared.height) }
        }
        
        struct Filter {
            let width: Int32
            let height: Int32
            let iconSize: Int32
            
            init() {
                let tokensSizesFilter = DesignTokens.SizesFilter.shared
                self.width = tokensSizesFilter.width
                self.height = tokensSizesFilter.height
                self.iconSize = tokensSizesFilter.iconSize
            }
        }
        
        struct FilterUI {
            var width: CGFloat { CGFloat(DesignTokens.SizesFilter.shared.width) }
            var height: CGFloat { CGFloat(DesignTokens.SizesFilter.shared.height) }
            var iconSize: CGFloat { CGFloat(DesignTokens.SizesFilter.shared.iconSize) }
        }
        
        let icon = Icon()
        let iconUI = IconUI()
        let cardRestaurant = CardRestaurant()
        let cardRestaurantUI = CardRestaurantUI()
        let cardDetail = CardDetail()
        let cardDetailUI = CardDetailUI()
        let filter = Filter()
        let filterUI = FilterUI()
    }
    
    struct ElevationTokens {
        let card: String
        let filter: String
        
        init() {
            let tokensElevation = DesignTokens.Elevation.shared
            self.card = tokensElevation.card
            self.filter = tokensElevation.filter
        }
    }
    
    let color = ColorTokens()
    let colorUI = ColorUITokens()
    let typography = TypographyTokens()
    let spacing = SpacingTokens()
    let spacingUI = SpacingUITokens()
    let borderRadius = BorderRadiusTokens()
    let borderRadiusUI = BorderRadiusUITokens()
    let size = SizeTokens()
    let elevation = ElevationTokens()
}
