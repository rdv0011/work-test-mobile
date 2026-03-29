package io.umain.munchies.navigation

import io.umain.munchies.navigation.DeepLinkResult

/**
 * Interface for deep link handlers. Each feature implements its own handler.
 */
interface DeepLinkHandler {
    /** Return true if this handler can parse the given deep link */
    fun canHandle(deepLink: String): Boolean
    /** Parse URL to navigation state */
    fun parseDeepLink(deepLink: String): DeepLinkResult
}
