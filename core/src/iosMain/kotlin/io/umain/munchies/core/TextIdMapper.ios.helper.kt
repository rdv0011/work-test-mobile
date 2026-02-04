package io.umain.munchies.core

import io.umain.munchies.core.ui.TextId
import io.umain.munchies.core.ui.mapTextIdToKey

fun mapTextIdToLocalizableKey(textId: TextId): String {
    return mapTextIdToKey(textId)
}
