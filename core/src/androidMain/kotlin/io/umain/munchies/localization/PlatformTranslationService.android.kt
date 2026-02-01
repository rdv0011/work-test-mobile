package io.umain.munchies.localization

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

actual class PlatformTranslationService actual constructor() : TranslationService, KoinComponent {
    
    private val context: Context by inject()
    
    override fun translate(key: TranslationKey, vararg args: Any): String {
        val resourceId = getResourceIdForKey(key)
        
        return if (resourceId != 0) {
            if (args.isEmpty()) {
                context.getString(resourceId)
            } else {
                context.getString(resourceId, *args)
            }
        } else {
            key
        }
    }
    
    override fun getCurrentLocale(): String {
        return Locale.getDefault().language
    }
    
    private fun getResourceIdForKey(key: String): Int {
        val resourceName = key.replace(".", "_")
        return context.resources.getIdentifier(resourceName, "string", context.packageName)
    }
}
