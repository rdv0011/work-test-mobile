//
//  NavigationEventCollector.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-02-01.
//
import shared

class FlowValuesCollector<T>: shared.FlowCollector {
    
    let callback: (T) -> Void
    
    init(callback: @escaping (T) -> Void) {
        self.callback = callback
    }
    
    func emit(value: Any?) async throws {
        await MainActor.run {
            guard let typedValue = value as? T else { return }
            self.callback(typedValue)
        }
    }
}
