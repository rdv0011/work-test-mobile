package io.umain.munchies.android.core.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import io.umain.munchies.core.di.KmpScopeId
import io.umain.munchies.core.viewmodel.ScopedViewModel
import io.umain.munchies.core.viewmodel.scopedViewModel

/**
 * Composable helper that creates a scoped ViewModel and manages its lifecycle.
 * Returns the ViewModel directly, handling scope creation and cleanup automatically.
 */
@Composable
inline fun <reified VM : ScopedViewModel> rememberScopedViewModel(
    scopeId: KmpScopeId,
    vararg params: Any?
): VM {
    val handle = remember(scopeId.value, *params) {
        scopedViewModel<VM>(scopeId, params.toList())
    }

    DisposableEffect(handle) {
        onDispose {
            handle.scope.close()
            handle.viewModel.close()
        }
    }

    return handle.viewModel
}