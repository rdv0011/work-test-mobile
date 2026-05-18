//
//  AsyncStream+StateFlow.swift
//  iosApp
//
//  Created by Dmitry Rybakov on 2026-02-02.
//
import shared
import os.log

typealias ViewModelState = CoreViewModelState
typealias LifecycleOwner = CoreLifecycleOwner

private let logger = Logger(subsystem: "com.munchies.ios", category: "StateFlow")

func asyncStateStream<S, VM: ViewModelState>(_ viewModel: VM) -> AsyncStream<S> {
    logger.debug("asyncStateStream: Starting stream for type: \(String(describing: S.self))")
    
    return AsyncStream<S> { continuation in
        logger.debug("asyncStateStream: AsyncStream continuation created")
        
        var isTerminated = false
        
        let job = IosAggregatorExportsKt.subscribeToStateFlow(
            lifecycle: viewModel as! LifecycleOwner,
            stateFlow: viewModel.stateFlow,
            onStateChanged: { (value: Any?) in
                // Guard against yields after termination
                guard !isTerminated else {
                    logger.debug("asyncStateStream: Skipping yield - continuation already terminated")
                    return
                }
                
                logger.debug("asyncStateStream: onStateChanged callback called with value: \(String(describing: value))")
                logger.debug("asyncStateStream: value type: \(type(of: value))")
                logger.debug("asyncStateStream: attempting cast to \(String(describing: S.self))")
                
                if let typed = value as? S {
                    logger.debug("asyncStateStream: ✓ Cast succeeded! Yielding value")
                    continuation.yield(typed)
                } else {
                    logger.warning("asyncStateStream: ✗ Cast FAILED! Expected \(String(describing: S.self)) but got \(type(of: value))")
                    logger.warning("asyncStateStream: value description: \(String(describing: value))")
                }
            }
        )
        
        logger.debug("asyncStateStream: Job created, setting up termination handler")
        
        continuation.onTermination = { _ in
            logger.debug("asyncStateStream: Continuation terminated, marking as terminated and cancelling job")
            isTerminated = true
            job.cancel(cause: nil)
        }
    }
}
