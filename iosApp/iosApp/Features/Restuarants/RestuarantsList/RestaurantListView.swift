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
            Text(tr("app.title"))
                .font(.largeTitle)
                .padding()
            
            Text(tr("restaurant.list.title"))
                .font(.headline)
            
            Button("View Restaurant Detail") {
                logInfo(tag: "RestaurantList", message: "Navigating to restaurant detail")
                coordinator.navigateToRestaurantDetail(restaurantId: "test-123")
            }
            .padding()
        }
        .navigationTitle(tr("restaurant.list.title"))
        .onAppear {
            logInfo(tag: "RestaurantList", message: "Restaurant list view appeared")
        }
    }
}
