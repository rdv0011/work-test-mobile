package io.umain.munchies.localization

import android.content.Context
import io.umain.munchies.core.TextId
import io.umain.munchies.core.mapTextIdToStringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Locale

actual class PlatformTranslationService actual constructor() : TranslationService, KoinComponent {
    
    private val context: Context by inject()
    
    override fun translate(textId: TextId, vararg args: Any): String {
        val resourceId = mapTextIdToStringResource(textId)
        
        return if (resourceId != 0) {
            if (args.isEmpty()) {
                context.getString(resourceId)
            } else {
                context.getString(resourceId, *args)
            }
        } else {
            textId::class.simpleName ?: "unknown"
        }
    }
    
    override fun getCurrentLocale(): String {
        return Locale.getDefault().language
    }
}
