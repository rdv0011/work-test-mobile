package io.umain.munchies.network

import io.ktor.client.engine.android.Android
import io.ktor.client.engine.HttpClientEngine

actual fun provideHttpClientEngine(): HttpClientEngine = Android.create()
