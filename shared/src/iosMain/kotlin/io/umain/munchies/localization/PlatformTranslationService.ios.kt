package io.umain.munchies.localization

import platform.Foundation.NSBundle
import platform.Foundation.NSLocale
import platform.Foundation.NSString
import platform.Foundation.currentLocale
import platform.Foundation.languageCode
import platform.Foundation.localizedStringWithFormat

actual class PlatformTranslationService actual constructor() : TranslationService {
    
    override fun translate(key: TranslationKey, vararg args: Any): String {
        val localizedString = NSBundle.mainBundle.localizedStringForKey(
            key = key,
            value = key,
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
