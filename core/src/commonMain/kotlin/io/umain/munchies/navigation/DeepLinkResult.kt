package io.umain.munchies.navigation

/**
 * Result of deep link parsing.
 */
sealed class DeepLinkResult {
    data class Success(
        val navigationState: NavigationState,
        val clearCurrentStack: Boolean = true
    ) : DeepLinkResult()

    data class Partial(
        val navigationState: NavigationState,
        val failedSegments: List<String>,
        val clearCurrentStack: Boolean = false
    ) : DeepLinkResult()

    data class NotFound(val link: String) : DeepLinkResult()
    data class Error(val link: String, val exception: Exception) : DeepLinkResult()
}
