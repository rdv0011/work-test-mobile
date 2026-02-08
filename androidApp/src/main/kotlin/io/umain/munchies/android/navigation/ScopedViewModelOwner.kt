package io.umain.munchies.android.navigation

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import io.umain.munchies.core.lifecycle.Closeable
import org.koin.core.scope.Scope

class ScopedViewModelOwner(
    val scope: Scope
) : ViewModelStoreOwner, Closeable {

    private val store = ViewModelStore()

    override val viewModelStore: ViewModelStore
        get() = store

    override fun close() {
        store.clear()
        scope.close()
    }
}
