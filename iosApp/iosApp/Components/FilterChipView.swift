//
//  FilterChipView.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-02-01.
//
import SwiftUI
import shared

/// FilterChipView - SwiftUI wrapper for FilterChipData
///
/// Displays a selectable filter chip with:
/// - Remote icon URL (48x48pt square)
/// - Label text (title2/Poppins medium style)
/// - Selection state with visual feedback
///
/// Dimensions: 144x48pt with 24pt border radius
/// Unselected: semi-transparent white (#FFFFFF66) + dark text
/// Selected: accent color (#E2A364) + light text
struct FilterChipView: View {
    let data: FilterChipData
    @State private var isSelected: Bool
    var onSelectionChanged: ((Bool) -> Void)? = nil
    
    init(data: FilterChipData, onSelectionChanged: ((Bool) -> Void)? = nil) {
        self.data = data
        self.onSelectionChanged = onSelectionChanged
        _isSelected = State(initialValue: data.isSelected)
    }
    
    var body: some View {
        HStack(spacing: .spacingUI.sm) {
            // Icon - 48pt square loaded from remote URL
            AsyncImage(url: URL(string: data.iconUrl)) { phase in
                switch phase {
                case .empty:
                    ZStack {
                        Color.gray.opacity(0.2)
                        ProgressView()
                            .tint(Color.gray)
                    }
                    .frame(width: .filterIconSize, height: .filterIconSize)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .accessibilityLabel("Loading filter icon")
                    
                case .success(let image):
                    image
                        .resizable()
                        .scaledToFill()
                        .frame(width: .filterIconSize, height: .filterIconSize)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                        .accessibilityLabel("Filter icon for \(data.label)")
                    
                case .failure:
                    ZStack {
                        Color.gray.opacity(0.2)
                        Image(systemName: "photo.fill")
                            .font(.system(size: 20))
                            .foregroundColor(.gray)
                    }
                    .frame(width: .filterIconSize, height: .filterIconSize)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .accessibilityLabel("Failed to load filter icon")
                    
                @unknown default:
                    EmptyView()
                }
            }
            
            // Label - title2 style (Poppins medium)
            Text(data.label)
                .font(.title2)
                .foregroundColor(isSelected ? .text.light : .text.picto)
                .lineLimit(1)
                .accessibilityLabel(data.contentDescription)
        }
        .frame(width: .filterWidth, height: .filterHeight)
        .background(isSelected ? Color.accent.selected : Color.background.filterDefault)
        .cornerRadius(.borderRadiusUI.full)
        .shadow(color: Color.black.opacity(0.04), radius: 2, x: 0, y: 2)
        .contentShape(Rectangle())
        .onTapGesture {
            withAnimation(.easeInOut(duration: 0.2)) {
                isSelected.toggle()
            }
            onSelectionChanged?(isSelected)
        }
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
        .accessibilityHint(isSelected ? "Selected" : "Not selected")
    }
}

// MARK: - Preview
#Preview {
    VStack(spacing: 16) {
        FilterChipView(
            data: FilterChipData(
                id: "filter-1",
                label: "Italian",
                iconUrl: "https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=48&h=48&fit=crop",
                isSelected: false,
                contentDescription: "Filter: Italian"
            )
        )
        
        FilterChipView(
            data: FilterChipData(
                id: "filter-2",
                label: "Sushi",
                iconUrl: "https://images.unsplash.com/photo-1579584425555-c3ce17fd4351?w=48&h=48&fit=crop",
                isSelected: true,
                contentDescription: "Filter: Sushi"
            )
        )
    }
    .padding()
}
