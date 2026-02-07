package io.umain.munchies.core.viewmodel

import io.umain.munchies.core.di.KmpScopeId
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named
import org.koin.mp.KoinPlatform
import kotlin.reflect.KClass

fun <VM : ScopedViewModel> scopedViewModel(
    vmClass: KClass<VM>,
    scopeId: KmpScopeId,
    params: List<Any?> = emptyList()
): ScopedViewModelHandle<VM> {

    val koin = KoinPlatform.getKoin()

    val scope = koin.getScopeOrNull(scopeId.value)
        ?: koin.createScope(
            scopeId = scopeId.value,
            qualifier = named(scopeId.qualifierName)
        )

    val viewModel: VM = scope.get(vmClass) {
        parametersOf(*params.toTypedArray())
    }

    return ScopedViewModelHandle(
        scope = scope,
        viewModel = viewModel
    )
}

inline fun <reified VM : ScopedViewModel> scopedViewModel(
    scopeId: KmpScopeId,
    params: List<Any?> = emptyList()
): ScopedViewModelHandle<VM> =
    scopedViewModel(
        vmClass = VM::class,
        scopeId = scopeId,
        params = params
    )