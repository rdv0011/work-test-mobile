package io.umain.munchies.core.localization

interface StringResourceProvider {
    fun stringResource(key: StringKey, vararg args: Any): String
    fun pluralResource(key: StringKey, quantity: Int, vararg args: Any): String
}

expect fun getStringResourceProvider(): StringResourceProvider
