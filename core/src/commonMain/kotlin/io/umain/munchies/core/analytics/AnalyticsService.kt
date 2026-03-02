package io.umain.munchies.core.analytics

interface AnalyticsService {
    suspend fun trackEvent(event: AnalyticsEvent)

    suspend fun setUserProperties(properties: Map<String, String>)

    suspend fun clearUserProperties()

    suspend fun flush()
}

class NoOpAnalyticsService : AnalyticsService {
    override suspend fun trackEvent(event: AnalyticsEvent) {}

    override suspend fun setUserProperties(properties: Map<String, String>) {}

    override suspend fun clearUserProperties() {}

    override suspend fun flush() {}
}
