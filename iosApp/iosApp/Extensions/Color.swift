//
//  Color.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-02-01.
//
import SwiftUI

extension Color {
    /// Initialize Color from hex string.
    /// Supports "#RRGGBB" and "#RRGGBBAA"
    init(hex: String) {
        // Remove leading "#" if present
        let hex = hex.trimmingCharacters(in: CharacterSet(charactersIn: "#"))
        
        // Convert hex string to UInt64
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        
        let r, g, b, a: Double
        
        if hex.count == 6 {
            // #RRGGBB
            r = Double((int >> 16) & 0xFF) / 255.0
            g = Double((int >> 8) & 0xFF) / 255.0
            b = Double(int & 0xFF) / 255.0
            a = 1.0
        } else if hex.count == 8 {
            // #RRGGBBAA
            r = Double((int >> 24) & 0xFF) / 255.0
            g = Double((int >> 16) & 0xFF) / 255.0
            b = Double((int >> 8) & 0xFF) / 255.0
            a = Double(int & 0xFF) / 255.0
        } else {
            // Invalid hex, default to black
            r = 0; g = 0; b = 0; a = 1.0
        }
        
        self.init(red: r, green: g, blue: b, opacity: a)
    }
}
