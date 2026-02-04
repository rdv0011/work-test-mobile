//
//  AsyncStream+StateFlow.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-02-02.
//
import shared

func asyncStateStream<S, VM: ViewModelState>(_ viewModel: VM) -> AsyncStream<S> {
    AsyncStream { continuation in
        let job = (viewModel as? KmpViewModel)?.subscribeState(viewModel.stateFlow) { value in
            if let typed = value as? S {
                continuation.yield(typed)
            }
        }
        continuation.onTermination = { _ in
            job?.cancel(cause: nil)
        }
    }
}
