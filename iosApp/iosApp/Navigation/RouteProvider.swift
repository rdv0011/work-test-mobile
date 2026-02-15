//
//  RouteProvider.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-02-14.
//

import Foundation
import shared

/**
 * iOS protocol for route providers.
 *
 * Features implement this protocol to declare all routes they provide.
 * The app layer uses this to dynamically discover routes without
 * needing to know about specific route types.
 */
protocol RouteProvider {
    /// Return all route handlers this feature provides
    func getRoutes() -> [RouteHandler]

    /// Check if this provider can handle the given destination
    func canHandle(destination: shared.Destination) -> Bool

    /// Convert a destination to a route if this provider handles it
    func destinationToRoute(destination: shared.Destination) -> shared.Route?
    
    /// Get the ViewModel holder for a route if this provider handles it
    func getHolder(for route: shared.Route) -> AnyObject?
}

/// Extension to provide default implementations
extension RouteProvider {
    func canHandle(destination: shared.Destination) -> Bool {
        return getRoutes().contains { $0.canHandle(destination: destination) }
    }

    func destinationToRoute(destination: shared.Destination) -> shared.Route? {
        return getRoutes().first { $0.canHandle(destination: destination) }?
            .destinationToRoute(destination: destination)
    }
}
