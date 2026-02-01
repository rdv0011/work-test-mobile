package io.umain.munchies.localization

import android.content.Context
import java.util.Locale

actual class PlatformTranslationService actual constructor() : TranslationService {
    
    private val context: Context = getApplicationContext()
    
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
    
    companion object {
        private lateinit var appContext: Context
        
        fun init(context: Context) {
            appContext = context.applicationContext
        }
        
        private fun getApplicationContext(): Context {
            if (!::appContext.isInitialized) {
                throw IllegalStateException(
                    "PlatformTranslationService not initialized. Call PlatformTranslationService.init(context) first."
                )
            }
            return appContext
        }
    }
}
