import SwiftUI
import shared

extension Font {
    static var headline1: Font {
        let dt = DesignTokens.iOS
        return .custom(dt.typography.fontFamily.helvetica, size: dt.typography.fontSizeUI.headline1)
    }
    
    static var title1: Font {
        let dt = DesignTokens.iOS
        return .custom(dt.typography.fontFamily.helvetica, size: dt.typography.fontSizeUI.title1)
    }
    
    static var headline2: Font {
        let dt = DesignTokens.iOS
        return .custom(dt.typography.fontFamily.helvetica, size: dt.typography.fontSizeUI.headline2)
    }
    
    static var title2: Font {
        let dt = DesignTokens.iOS
        return .custom(dt.typography.fontFamily.poppins, size: dt.typography.fontSizeUI.title2)
    }
    
    static var subtitle1: Font {
        let dt = DesignTokens.iOS
        return .custom(dt.typography.fontFamily.helvetica, size: dt.typography.fontSizeUI.subtitle1)
            .weight(.bold)
    }
    
    static var footer1: Font {
        let dt = DesignTokens.iOS
        return .custom(dt.typography.fontFamily.inter, size: dt.typography.fontSizeUI.footer1)
            .weight(.medium)
    }
}
