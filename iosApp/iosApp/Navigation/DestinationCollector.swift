//
//  DestinationCollector.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-01-31.
//
import shared

class DestinationCollector: shared.FlowCollector {
    weak var observer: NavigationStateObserver?

    init(observer: NavigationStateObserver) {
        self.observer = observer
    }

    func emit(value: Any?) async throws {
        guard let dest = value as? shared.Destination,
              let observer = observer else { return }

        // Hop onto the main actor explicitly to update @Published
        await MainActor.run {
            observer.currentDestination = dest
        }
    }
}
