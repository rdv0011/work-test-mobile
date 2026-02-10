package io.umain.munchies.core.viewmodel

import io.umain.munchies.core.di.KmpScopeId
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.mp.KoinPlatform
import kotlin.reflect.KClass

/**
 * Access a ViewModel from an existing scope.
 *
 * CRITICAL: This is NOT a scope creator. It assumes the scope already exists
 * (created by RouteRegistry). Throws an error if the scope doesn't exist.
 *
 * OWNERSHIP MODEL:
 * - RouteRegistry OWNS the scope lifetime
 * - This function is a pure ACCESSOR
 * - Features do NOT own scopes
 *
 * MUST BE CALLED AFTER: RouteRegistry has created the scope
 *
 * @param vmClass The ViewModel class to retrieve from the scope
 * @param scopeId The scope identity (must exist)
 * @param params Optional parameters to pass to the ViewModel constructor
 * @return Handle to the ViewModel (scoped to its RouteLifetime)
 * @throws IllegalStateException if scope doesn't exist
 */
fun <VM : ScopedViewModel> scopedViewModel(
    vmClass: KClass<VM>,
    scopeId: KmpScopeId,
    params: List<Any?> = emptyList()
): ScopedViewModelHandle<VM> {

    val koin = KoinPlatform.getKoin()

    val scope = koin.getScopeOrNull(scopeId.value)
        ?: error("Scope '${scopeId.value}' not found. Route Registry must create it first. This is a pure accessor, not a scope creator.")

    val viewModel: VM = scope.get(vmClass) {
        parametersOf(*params.toTypedArray())
    }

    return ScopedViewModelHandle(
        scope = scope,
        viewModel = viewModel
    )
}

/**
 * Reified overload for convenience (type parameter derived from context).
 * @see scopedViewModel
 */
inline fun <reified VM : ScopedViewModel> scopedViewModel(
    scopeId: KmpScopeId,
    params: List<Any?> = emptyList()
): ScopedViewModelHandle<VM> =
    scopedViewModel(
        vmClass = VM::class,
        scopeId = scopeId,
        params = params
    )
