package io.umain.munchies.core

sealed class IconId {
    object Logo : IconId()
    object Clock : IconId()
    object Star : IconId()
}
