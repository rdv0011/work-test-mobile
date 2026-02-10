package io.umain.munchies.core.di

import org.koin.core.scope.Scope
import org.koin.core.parameter.parametersOf
import kotlin.reflect.KClass

/**
 * iOS-specific ViewModel retrieval from Scope.
 *
 * Swift cannot directly call generic Kotlin functions like scope.get<T>(),
 * so this provides stable non-generic entry points that iOS can call.
 *
 * Usage from Swift:
 * ```swift
 * let viewModel: RestaurantDetailViewModel = scope.getViewModelTyped() as! RestaurantDetailViewModel
 * ```
 */

/**
 * Generic non-reified wrapper for getting ViewModels without type parameters.
 * Used internally by iOS platform-specific code.
 */
fun Scope.getViewModelTyped(type: KClass<*>): Any = get(clazz = type)

/**
 * Generic wrapper for getting ViewModels with parameters.
 * Used internally by iOS platform-specific code.
 */
fun Scope.getViewModelTyped(type: KClass<*>, vararg parameters: Any): Any =
    get(clazz = type, parameters = { parametersOf(*parameters) })
