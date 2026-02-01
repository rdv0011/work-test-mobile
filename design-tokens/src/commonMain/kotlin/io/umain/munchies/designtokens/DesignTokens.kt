package io.umain.munchies.designtokens

/**
 * Design Tokens - Single source of truth for all UI values
 * Generated from tokens.json
 * 
 * DO NOT use hardcoded values outside this file.
 * Platform-specific units (dp, pt) applied at render time.
 */

object DesignTokens {
    
    object Colors {
        object Text {
            const val dark = "#1F2B2E"
            const val light = "#FFFFFF"
            const val subtitle = "#999999"
            const val footer = "#50555C"
            const val picto = "#000000"
        }
        
        object Background {
            const val primary = "#F8F8F8"
            const val card = "#FFFFFF"
            const val filterDefault = "rgba(255, 255, 255, 0.4)"
        }
        
        object Accent {
            const val selected = "#E2A364"
            const val positive = "#2ECC71"
            const val negative = "#C0392B"
            const val star = "#F9CA24"
            const val brightRed = "#FF5252"
        }
    }
    
    object Typography {
        object FontFamilies {
            const val helvetica = "Helvetica"
            const val poppins = "Poppins"
            const val inter = "Inter"
        }
        
        object FontWeights {
            const val regular = 400
            const val medium = 500
            const val bold = 700
        }
        
        object FontSizes {
            const val headline1 = 24
            const val title1 = 18
            const val headline2 = 16
            const val title2 = 14
            const val subtitle1 = 12
            const val footer1 = 10
        }
        
        object LineHeights {
            const val headline1 = 16
            const val title1 = 16
            const val headline2 = 16
            const val title2 = 20
            const val subtitle1 = 16
            const val footer1 = 12
        }
        
        data class TextStyle(
            val fontFamily: String,
            val fontWeight: Int,
            val fontSize: Int,
            val lineHeight: Int
        )
        
        object TextStyles {
            val headline1 = TextStyle(
                fontFamily = FontFamilies.helvetica,
                fontWeight = FontWeights.regular,
                fontSize = FontSizes.headline1,
                lineHeight = LineHeights.headline1
            )
            
            val title1 = TextStyle(
                fontFamily = FontFamilies.helvetica,
                fontWeight = FontWeights.regular,
                fontSize = FontSizes.title1,
                lineHeight = LineHeights.title1
            )
            
            val headline2 = TextStyle(
                fontFamily = FontFamilies.helvetica,
                fontWeight = FontWeights.regular,
                fontSize = FontSizes.headline2,
                lineHeight = LineHeights.headline2
            )
            
            val title2 = TextStyle(
                fontFamily = FontFamilies.poppins,
                fontWeight = FontWeights.medium,
                fontSize = FontSizes.title2,
                lineHeight = LineHeights.title2
            )
            
            val subtitle1 = TextStyle(
                fontFamily = FontFamilies.helvetica,
                fontWeight = FontWeights.bold,
                fontSize = FontSizes.subtitle1,
                lineHeight = LineHeights.subtitle1
            )
            
            val footer1 = TextStyle(
                fontFamily = FontFamilies.inter,
                fontWeight = FontWeights.medium,
                fontSize = FontSizes.footer1,
                lineHeight = LineHeights.footer1
            )
        }
    }
    
    object Spacing {
        const val none = 0
        const val xxs = 2
        const val xs = 3
        const val sm = 8
        const val md = 13
        const val lg = 16
        const val xl = 18
    }
    
    object BorderRadius {
        const val none = 0
        const val sm = 0.8f
        const val md = 12
        const val full = 24
    }
    
    object Elevation {
        const val card = "0px 4px 4px rgba(0, 0, 0, 0.1)"
        const val filter = "0px 4px 10px rgba(0, 0, 0, 0.04)"
    }
    
    object Sizes {
        object Icon {
            const val small = 10
            const val medium = 12
            const val large = 48
        }
        
        object Card {
            object Restaurant {
                const val width = 343
                const val height = 196
                const val imageHeight = 132
            }
            
            object Detail {
                const val width = 343
                const val height = 144
            }
        }
        
        object Filter {
            const val width = 144
            const val height = 48
            const val iconSize = 48
        }
    }
}
