//
//  RestaurantListView.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-01-31.
//
import SwiftUI
import shared

struct RestaurantListView: View {
    let coordinator: AppCoordinator
    
    var body: some View {
        VStack {
            Text(tr(TextIdAppTitle))
                .font(.largeTitle)
                .padding()
            
            Text(tr(TextIdRestaurantListTitle))
                .font(.headline)
            
            Button("View Restaurant Detail") {
                logInfo(tag: "RestaurantList", message: "Navigating to restaurant detail")
                coordinator.navigateToRestaurantDetail(restaurantId: "test-123")
            }
            .padding()
        }
        .navigationTitle(tr(TextIdRestaurantListTitle))
        .onAppear {
            logInfo(tag: "RestaurantList", message: "Restaurant list view appeared")
        }
    }
}
