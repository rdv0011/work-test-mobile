package io.umain.munchies.localization

import io.umain.munchies.core.ui.TextId
import io.umain.munchies.core.mapTextIdToLocalizableKey
import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual class PlatformTranslationService actual constructor() : TranslationService {
    
    override fun translate(textId: TextId, vararg args: Any): String {
        val localizableKey = mapTextIdToLocalizableKey(textId)
        val localizedString = NSBundle.mainBundle.localizedStringForKey(
            key = localizableKey,
            value = localizableKey,
            table = null
        )
        
        return if (args.isEmpty()) {
            localizedString
        } else {
            formatString(localizedString, args)
        }
    }
    
    override fun getCurrentLocale(): String {
        return NSLocale.currentLocale.languageCode
    }
    
    private fun formatString(template: String, args: Array<out Any>): String {
        var result = template
        args.forEach { arg ->
            result = result.replaceFirst("%s", arg.toString())
                .replaceFirst("%d", arg.toString())
        }
        return result
    }
}
