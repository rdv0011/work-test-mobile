package io.umain.munchies.core

import android.content.Context
import io.umain.munchies.core.ui.TextId
import io.umain.munchies.core.ui.mapTextIdToKey

fun mapTextIdToStringResource(textId: TextId): Int {
    val context: Context = try {
        org.koin.core.context.GlobalContext.get().get()
    } catch (e: Exception) {
        return 0
    }
    
    val key = mapTextIdToKey(textId)
    val resourceName = key.replace(".", "_")
    return context.resources.getIdentifier(resourceName, "string", context.packageName)
}
