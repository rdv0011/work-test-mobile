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
            Text(tr("restaurant.detail.title"))
                .font(.largeTitle)
                .padding()
            
            Text("ID: \(restaurantId)")
                .font(.headline)
            
            HStack(spacing: 20) {
                Text(tr("restaurant.status.open"))
                    .foregroundColor(.green)
                Text(tr("restaurant.status.closed"))
                    .foregroundColor(.red)
            }
            .padding()
            
            Button(tr("accessibility.back.button")) {
                logInfo(tag: "RestaurantDetail", message: "Navigating back from restaurant: \(restaurantId)")
                _ = coordinator.navigateBack()
            }
            .padding()
        }
        .navigationTitle(tr("restaurant.detail.title"))
        .onAppear {
            logInfo(tag: "RestaurantDetail", message: "Viewing restaurant detail: \(restaurantId)")
        }
    }
}
