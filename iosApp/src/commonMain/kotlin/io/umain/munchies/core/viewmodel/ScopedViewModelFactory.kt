package io.umain.munchies.core.viewmodel

import io.umain.munchies.core.di.KmpScopeId
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.TypeQualifier
import org.koin.mp.KoinPlatform

inline fun <reified VM : ScopedViewModel> scopedViewModel(
    scopeId: KmpScopeId,
    params: List<Any?> = emptyList()
): ScopedViewModelHandle<VM> {

    val scope = KoinPlatform.getKoin()
        .createScope(scopeId.value, TypeQualifier(scopeId::class))

    val viewModel = scope.get<VM> {
        parametersOf(*params.toTypedArray())
    }

    return ScopedViewModelHandle(scope, viewModel)
}