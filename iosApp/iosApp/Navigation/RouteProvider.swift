//
//  RouteProvider.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-02-14.
//

import Foundation
import shared

protocol RouteProvider {
    func getRoutes() -> [CoreRouteHandler]
    func canHandle(destination: CoreDestination) -> Bool
    func destinationToRoute(destination: CoreDestination) -> CoreRoute?
    func getHolder(for route: CoreRoute) -> AnyObject?
}

extension RouteProvider {
    func canHandle(destination: CoreDestination) -> Bool {
        return getRoutes().contains { $0.canHandle(destination: destination) }
    }

    func destinationToRoute(destination: CoreDestination) -> CoreRoute? {
        return getRoutes().first { $0.canHandle(destination: destination) }?
            .destinationToRoute(destination: destination)
    }
}
