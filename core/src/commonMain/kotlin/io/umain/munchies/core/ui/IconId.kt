package io.umain.munchies.core.ui

import kotlinx.serialization.Serializable

@Serializable
sealed class IconId {
    @Serializable
    object Logo : IconId()
    @Serializable
    object Clock : IconId()
    @Serializable
    object Star : IconId()
    @Serializable
    object Restaurant : IconId()
    @Serializable
    object Settings : IconId()
}
