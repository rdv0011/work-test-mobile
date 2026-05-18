package io.umain.munchies.core.lifecycle

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import io.umain.munchies.logging.logDebug
import io.umain.munchies.logging.logError

@OptIn(ExperimentalObjCName::class)
@ObjCName("subscribeToStateFlow")
fun <T> subscribeToStateFlow(
    lifecycle: LifecycleOwner,
    stateFlow: StateFlow<T>,
    onStateChanged: (T) -> Unit
): Job {
    logDebug("subscribeToStateFlow", "Starting subscription to StateFlow")
    logDebug("subscribeToStateFlow", "Current StateFlow value: ${stateFlow.value}")
    
    return lifecycle.getScope().launch {
        logDebug("subscribeToStateFlow", "Coroutine launched, starting collect")
        stateFlow.collect { value ->
            logDebug("subscribeToStateFlow", "✓ StateFlow emitted value: $value")
            logDebug("subscribeToStateFlow", "value type: ${value?.let { it::class.simpleName } ?: "null"}")
            logDebug("subscribeToStateFlow", "Calling onStateChanged callback")
            try {
                onStateChanged(value)
                logDebug("subscribeToStateFlow", "✓ onStateChanged callback completed successfully")
            } catch (e: Exception) {
                logError("subscribeToStateFlow", "✗ onStateChanged callback threw exception: ${e.message}")
            }
        }
        logDebug("subscribeToStateFlow", "collect() completed (should not reach here until unsubscribed)")
    }
}
