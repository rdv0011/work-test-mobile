package io.umain.munchies.core

import android.content.Context

class IconIdMapper(private val context: Context) {
    fun mapToDrawableResourceId(iconId: IconId): Int {
        return when (iconId) {
            IconId.Logo -> context.resources.getIdentifier("ic_logo", "drawable", context.packageName)
            IconId.Clock -> context.resources.getIdentifier("ic_clock_icon", "drawable", context.packageName)
            IconId.Star -> context.resources.getIdentifier("ic_star_icon", "drawable", context.packageName)
        }
    }
}
