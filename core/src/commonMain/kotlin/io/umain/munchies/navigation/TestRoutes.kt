package io.umain.munchies.navigation

import kotlinx.serialization.Serializable

@Serializable
data class TestRoute(
    override val key: String,
    override val isRootRoute: Boolean = false
) : StackRoute()

@Serializable
data class TestModalRoute(
    override val key: String
) : ModalRoute() {
    override val presentationStyle: ModalPresentationStyle = ModalPresentationStyle.SHEET
}
