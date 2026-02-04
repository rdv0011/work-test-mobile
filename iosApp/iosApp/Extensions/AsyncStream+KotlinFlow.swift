//
//  AsyncStream+KotlinFlow.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-02-02.
//
import shared

@MainActor
func asyncKotlinStream<T>(_ flow: any Flow) -> AsyncStream<T> {
    AsyncStream { continuation in
        Task {
            do {
                try await flow.collect(
                    collector: FlowValuesCollector(
                        callback: { value in
                            continuation.yield(value)
                        }))
            } catch {
                // Flow threw, stop the AsyncStream
                continuation.finish()
            }
        }
    }
}
