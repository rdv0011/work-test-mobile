package io.umain.munchies.core

import io.umain.munchies.core.ui.IconId

class IconIdMapper {
    fun mapToFileName(iconId: IconId): String {
        return when (iconId) {
            IconId.Logo -> "ic_logo.svg"
            IconId.Clock -> "ic_clock_icon.svg"
            IconId.Star -> "ic_star_icon.svg"
        }
    }
    
    fun mapToResourcePath(iconId: IconId): String {
        return "Icons/" + mapToFileName(iconId)
    }
}
