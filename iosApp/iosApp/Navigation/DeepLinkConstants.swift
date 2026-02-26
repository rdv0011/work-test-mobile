//
//  DeepLinkConstants.swift
//  iosApp
//
//  Constants for deep link handling across the application.
//  Centralizes all deep link-related string literals and patterns.
//

import Foundation

struct DeepLinkConstants {
    // MARK: - Scheme
    static let scheme = "munchies"
    
    // MARK: - Hosts
    static let hostRestaurants = "restaurants"
    static let hostModal = "modal"
    static let hostSettings = "settings"
    
    // MARK: - Paths
    static let pathFilter = "filter"
    static let pathSubmitReview = "submit_review"
    static let pathConfirm = "confirm"
    static let pathDatePicker = "date_picker"
    
    // MARK: - Query Parameter Names
    static let queryParamFilters = "filters"
    static let queryParamMessage = "message"
    static let queryParamConfirmText = "confirmText"
    static let queryParamCancelText = "cancelText"
    static let queryParamInitialDate = "initialDate"
    
    // MARK: - Default Values
    static let defaultConfirmMessage = "Are you sure?"
    static let defaultConfirmText = "OK"
    static let defaultCancelText = "Cancel"
    
    // MARK: - Tab IDs
    static let tabIdRestaurants = "restaurants"
    static let tabIdSettings = "settings"
    
    // MARK: - Path Segment Indices
    static let modalTypeIndex = 0
    static let restaurantIdIndex = 0
    static let submitReviewRestaurantIdIndex = 1
}
