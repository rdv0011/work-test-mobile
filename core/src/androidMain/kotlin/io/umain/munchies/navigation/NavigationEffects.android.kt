package io.umain.munchies.navigation

import android.util.Log
import io.umain.munchies.core.lifecycle.Closeable
import org.koin.core.context.GlobalContext
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope

actual fun getKoinScopeOrNull(scopeId: String): Closeable? {
    val koin = GlobalContext.getOrNull() ?: return null
    val scope = koin.getScopeOrNull(scopeId)
    Log.i("NavigationEffects.android", "getKoinScopeOrNull called for scopeId=$scopeId, found=${scope != null}")
    return scope?.let { AndroidKoinScopeCloseable(it, scopeId) }
}

private class AndroidKoinScopeCloseable(private val scope: Scope, private val scopeId: String) : Closeable {
    override fun close() {
        Log.i("NavigationEffects.android", "Closing Koin scope for scopeId=$scopeId")
        scope.getAll<Closeable>().forEach {
            try {
                it.close()
                Log.i("NavigationEffects.android", "Closed Closeable: ${it::class.qualifiedName}")
            } catch (e: Exception) {
                Log.w("NavigationEffects.android", "Failed to close ${it::class.qualifiedName}", e)
            }
        }
        scope.close()
    }
}

actual fun createKoinScope(scopeId: String, qualifier: String) {
    val koin = GlobalContext.getOrNull() ?: return
    koin.createScope(scopeId, named(qualifier))
}
