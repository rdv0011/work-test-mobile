package io.umain.munchies.navigation

/**
 * Handles deep link URL parsing and navigation state reconstruction using registered handlers.
 */
class DeepLinkParser(
    private val handlers: List<DeepLinkHandler>
) {
    /**
     * Parse a deep link URL and return the corresponding navigation state/result.
     */
    fun parse(deepLink: String): DeepLinkResult {
        for (handler in handlers) {
            if (handler.canHandle(deepLink)) {
                return handler.parseDeepLink(deepLink)
            }
        }
        return DeepLinkResult.NotFound(deepLink)
    }
}
