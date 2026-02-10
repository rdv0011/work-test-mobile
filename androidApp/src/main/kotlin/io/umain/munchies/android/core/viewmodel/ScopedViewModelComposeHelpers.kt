package io.umain.munchies.android.core.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.umain.munchies.android.navigation.LocalRouteRegistry
import io.umain.munchies.core.viewmodel.ScopedViewModel
import org.koin.core.scope.Scope

@Composable
inline fun <reified VM : ScopedViewModel> rememberScopedViewModel(
    routeId: String,
    noinline factory: () -> Scope
): VM {
    val registry = LocalRouteRegistry.current
    
    val lifetime = remember(routeId) {
        registry.lifetimeFor(routeId, factory)
    }
    
    return remember(lifetime) {
        lifetime.scope.get<VM>()
    }
}