package io.umain.munchies.core.lifecycle

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface Closeable {
    fun close()
}

@OptIn(ExperimentalStdlibApi::class)
abstract class KmpViewModel : Closeable {

    private val job = SupervisorJob()

    protected val scope =
        CoroutineScope(job + Dispatchers.Main.immediate)

    // Swift-friendly StateFlow adapter
    fun <T> StateFlow<T>.subscribeState(onEach: (T) -> Unit): kotlinx.coroutines.Job =
        scope.launch {
            collect { onEach(it) }
        }

    final override fun close() {
        scope.cancel()
        onCleared()
    }

    protected open fun onCleared() {}
}