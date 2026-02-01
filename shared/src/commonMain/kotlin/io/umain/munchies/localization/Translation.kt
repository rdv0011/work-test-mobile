package io.umain.munchies.localization

typealias TranslationKey = String

interface TranslationService {
    fun translate(key: TranslationKey, vararg args: Any): String
    fun getCurrentLocale(): String
}

expect class PlatformTranslationService() : TranslationService

private lateinit var translationServiceInstance: TranslationService

fun initTranslationService(service: TranslationService = PlatformTranslationService()) {
    translationServiceInstance = service
}

fun tr(key: TranslationKey, vararg args: Any): String {
    if (!::translationServiceInstance.isInitialized) {
        initTranslationService()
    }
    return translationServiceInstance.translate(key, *args)
}

fun getCurrentLocale(): String {
    if (!::translationServiceInstance.isInitialized) {
        initTranslationService()
    }
    return translationServiceInstance.getCurrentLocale()
}
