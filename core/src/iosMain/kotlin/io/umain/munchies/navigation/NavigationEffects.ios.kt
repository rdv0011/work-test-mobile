package io.umain.munchies.navigation

import org.koin.core.Koin
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import io.umain.munchies.core.lifecycle.Closeable
import io.umain.munchies.di.getKoin
import io.umain.munchies.logging.logInfo

actual fun getKoinScopeOrNull(scopeId: String): Closeable? {
    return try {
        val koin: Koin = getKoin()
        val scope: Scope? = try {
            koin.getScopeOrNull(scopeId)
        } catch (_: Exception) {
            null
        }
        logInfo("NavigationEffects.ios", "getKoinScopeOrNull called for scopeId=$scopeId, found=${scope != null}")
        scope?.let { IOSKoinScopeCloseable(it, scopeId) }
    } catch (_: Exception) {
        null
    }
}

private class IOSKoinScopeCloseable(private val scope: Scope, private val scopeId: String) : Closeable {
    override fun close() {
        logInfo("NavigationEffects.ios", "Closing Koin scope for scopeId=$scopeId")
        scope.close()
    }
}

actual fun createKoinScope(scopeId: String, qualifier: String) {
    try {
        val koin: Koin = getKoin()
        koin.createScope(scopeId, named(qualifier))
    } catch (_: Exception) {
    }
}

