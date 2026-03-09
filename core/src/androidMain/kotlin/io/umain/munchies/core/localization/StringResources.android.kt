package io.umain.munchies.core.localization

import android.content.Context

private var androidContext: Context? = null

fun setApplicationContext(context: Context) {
    androidContext = context
}

actual fun stringResource(key: StringKey, vararg args: Any): String {
    val context = androidContext ?: throw IllegalStateException("Application context not set. Call setApplicationContext() in MainActivity.onCreate()")
    return getStringResource(context, key, *args)
}

actual fun pluralResource(key: StringKey, quantity: Int, vararg args: Any): String {
    val context = androidContext ?: throw IllegalStateException("Application context not set. Call setApplicationContext() in MainActivity.onCreate()")
    return getPluralResource(context, key, quantity, *args)
}

private fun getStringResource(context: Context, key: String, vararg args: Any): String {
    return try {
        val resId = context.resources.getIdentifier(key, "string", context.packageName)
        if (resId != 0) {
            val format = context.getString(resId)
            if (args.isNotEmpty()) {
                String.format(format, *args)
            } else {
                format
            }
        } else {
            key
        }
    } catch (e: Exception) {
        key
    }
}

private fun getPluralResource(context: Context, key: String, quantity: Int, vararg args: Any): String {
    return try {
        val resId = context.resources.getIdentifier(key, "plurals", context.packageName)
        if (resId != 0) {
            val format = context.resources.getQuantityString(resId, quantity)
            val argsWithQuantity = arrayOf(quantity, *args)
            if (args.isNotEmpty()) {
                String.format(format, *argsWithQuantity)
            } else {
                format
            }
        } else {
            key
        }
    } catch (e: Exception) {
        key
    }
}
