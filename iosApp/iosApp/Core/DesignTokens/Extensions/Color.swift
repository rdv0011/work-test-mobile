import SwiftUI
import shared

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet(charactersIn: "#"))
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        
        let r, g, b, a: Double
        
        if hex.count == 6 {
            r = Double((int >> 16) & 0xFF) / 255.0
            g = Double((int >> 8) & 0xFF) / 255.0
            b = Double(int & 0xFF) / 255.0
            a = 1.0
        } else if hex.count == 8 {
            r = Double((int >> 24) & 0xFF) / 255.0
            g = Double((int >> 16) & 0xFF) / 255.0
            b = Double((int >> 8) & 0xFF) / 255.0
            a = Double(int & 0xFF) / 255.0
        } else {
            r = 0; g = 0; b = 0; a = 1.0
        }
        
        self.init(red: r, green: g, blue: b, opacity: a)
    }
    
    static var text: ColorTextNamespace { ColorTextNamespace() }
    static var background: ColorBackgroundNamespace { ColorBackgroundNamespace() }
    static var accent: ColorAccentNamespace { ColorAccentNamespace() }
}

struct ColorTextNamespace {
    var dark: Color { DesignTokens.iOS.colorUI.text.dark }
    var light: Color { DesignTokens.iOS.colorUI.text.light }
    var subtitle: Color { DesignTokens.iOS.colorUI.text.subtitle }
    var footer: Color { DesignTokens.iOS.colorUI.text.footer }
    var picto: Color { DesignTokens.iOS.colorUI.text.picto }
}

struct ColorBackgroundNamespace {
    var primary: Color { DesignTokens.iOS.colorUI.background.primary }
    var card: Color { DesignTokens.iOS.colorUI.background.card }
    var filterDefault: Color { DesignTokens.iOS.colorUI.background.filterDefault }
}

struct ColorAccentNamespace {
    var selected: Color { DesignTokens.iOS.colorUI.accent.selected }
    var positive: Color { DesignTokens.iOS.colorUI.accent.positive }
    var negative: Color { DesignTokens.iOS.colorUI.accent.negative }
    var star: Color { DesignTokens.iOS.colorUI.accent.star }
    var brightRed: Color { DesignTokens.iOS.colorUI.accent.brightRed }
}
