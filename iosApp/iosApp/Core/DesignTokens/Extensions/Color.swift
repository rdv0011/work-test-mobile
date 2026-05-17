import SwiftUI

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
    var dark: Color { .black }
    var light: Color { .white }
    var subtitle: Color { .gray }
    var footer: Color { .gray }
    var picto: Color { .black }
}

struct ColorBackgroundNamespace {
    var primary: Color { .white }
    var card: Color { .white }
    var filterDefault: Color { .gray.opacity(0.1) }
}

struct ColorAccentNamespace {
    var selected: Color { .blue }
    var positive: Color { .green }
    var negative: Color { .red }
    var star: Color { .yellow }
    var brightRed: Color { .red }
}

extension CGFloat {
    static let spacingUI = SpacingUI()
    static let borderRadiusUI = BorderRadiusUI()
    
    static let cardRestaurantImageHeight: CGFloat = 132
    static let cardRestaurantWidth: CGFloat = 343
    static let cardRestaurantHeight: CGFloat = 232
    static let filterIconSize: CGFloat = 24
    static let filterWidth: CGFloat = 100
    static let filterHeight: CGFloat = 40
}

struct SpacingUI {
    let xs: CGFloat = 4
    let sm: CGFloat = 8
    let md: CGFloat = 12
    let lg: CGFloat = 16
    let xl: CGFloat = 24
}

struct BorderRadiusUI {
    let xs: CGFloat = 4
    let sm: CGFloat = 8
    let md: CGFloat = 12
    let lg: CGFloat = 16
    let full: CGFloat = 1000
}

