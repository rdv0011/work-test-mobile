package io.umain.munchies.network

import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.HttpClientEngine

actual fun provideHttpClientEngine(): HttpClientEngine = Darwin.create()
