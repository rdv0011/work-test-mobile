package io.umain.munchies.localization

import io.umain.munchies.core.TextId
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

typealias TranslationKey = String

interface TranslationService {
    fun translate(textId: TextId, vararg args: Any): String
    fun getCurrentLocale(): String
}

expect class PlatformTranslationService() : TranslationService

object TranslationHelper : KoinComponent {
    private val translationService: TranslationService by inject()
    
    fun translate(textId: TextId, vararg args: Any): String {
        return translationService.translate(textId, *args)
    }
    
    fun getCurrentLocale(): String {
        return translationService.getCurrentLocale()
    }
}

fun tr(textId: TextId, vararg args: Any): String {
    return TranslationHelper.translate(textId, *args)
}

fun getCurrentLocale(): String {
    return TranslationHelper.getCurrentLocale()
}
