//
//  RestaurantDetailView.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-01-31.
//
import SwiftUI
import shared

struct RestaurantDetailView: View {
    let restaurantId: String
    let coordinator: AppCoordinator
    
    var body: some View {
        VStack {
            Text(tr(TextIdRestaurantDetailTitle))
                .font(.largeTitle)
                .padding()
            
            Text("ID: \(restaurantId)")
                .font(.headline)
            
            HStack(spacing: 20) {
                Text(tr(TextIdRestaurantStatusOpen))
                    .foregroundColor(.green)
                Text(tr(TextIdRestaurantStatusClosed))
                    .foregroundColor(.red)
            }
            .padding()
            
            Button(tr(TextIdAccessibilityBackButton)) {
                logInfo(tag: "RestaurantDetail", message: "Navigating back from restaurant: \(restaurantId)")
                _ = coordinator.navigateBack()
            }
            .padding()
        }
        .navigationTitle(tr(TextIdRestaurantDetailTitle))
        .onAppear {
            logInfo(tag: "RestaurantDetail", message: "Viewing restaurant detail: \(restaurantId)")
        }
    }
}
