//
//  NavigationEventCollector.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-02-01.
//
import shared

class NavigationEventCollector: shared.FlowCollector {
    weak var navigator: NavigationCoordinator?
    
    init(navigator: NavigationCoordinator) {
        self.navigator = navigator
    }
    
    func emit(value: Any?) async throws {
        guard let event = value as? NavigationEvent,
              let navigator = navigator else { return }
        
        await MainActor.run {
            navigator.handle(event: event)
        }
    }
}
