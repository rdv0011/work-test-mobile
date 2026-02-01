import SwiftUI

/**
 * Shadow+DesignTokens Extension
 *
 * Elevation tokens from KMP DesignTokens:
 * - card:   "#0000001A" (0% black, 10% opacity)
 * - filter: "#0000000A" (0% black, 5% opacity)
 */

extension View {
    @ViewBuilder
    func cardElevation() -> some View {
        self.shadow(color: Color.black.opacity(0.1), radius: 2, x: 0, y: 2)
    }
    
    @ViewBuilder
    func filterElevation() -> some View {
        self.shadow(color: Color.black.opacity(0.05), radius: 1, x: 0, y: 1)
    }
}

struct ElevationShadow {
    static let card = ShadowDefinition(
        color: Color.black,
        opacity: 0.1,
        radius: 2,
        x: 0,
        y: 2
    )
    
    static let filter = ShadowDefinition(
        color: Color.black,
        opacity: 0.05,
        radius: 1,
        x: 0,
        y: 1
    )
}

struct ShadowDefinition {
    let color: Color
    let opacity: Double
    let radius: CGFloat
    let x: CGFloat
    let y: CGFloat
}
