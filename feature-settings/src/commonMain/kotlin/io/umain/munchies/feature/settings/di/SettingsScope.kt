package io.umain.munchies.feature.settings.di

import io.umain.munchies.core.di.KmpScopeId

object SettingsScope : KmpScopeId {
    override val value = "Settings"
    override val qualifierName: String = "SettingsScope"
}
