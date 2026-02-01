package io.umain.munchies.localization

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

typealias TranslationKey = String

interface TranslationService {
    fun translate(key: TranslationKey, vararg args: Any): String
    fun getCurrentLocale(): String
}

expect class PlatformTranslationService() : TranslationService

object TranslationHelper : KoinComponent {
    private val translationService: TranslationService by inject()
    
    fun translate(key: TranslationKey, vararg args: Any): String {
        return translationService.translate(key, *args)
    }
    
    fun getCurrentLocale(): String {
        return translationService.getCurrentLocale()
    }
}

fun tr(key: TranslationKey, vararg args: Any): String {
    return TranslationHelper.translate(key, *args)
}

fun getCurrentLocale(): String {
    return TranslationHelper.getCurrentLocale()
}
