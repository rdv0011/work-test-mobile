package io.umain.munchies.core.localization

expect fun stringResource(key: StringKey, vararg args: Any): String

expect fun pluralResource(key: StringKey, quantity: Int, vararg args: Any): String
