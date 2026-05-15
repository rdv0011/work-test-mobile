package versions

import org.gradle.api.JavaVersion

object Versions {
    // Kotlin & Gradle
    const val kotlin = "2.3.10"
    const val agp = "9.0.0"
    const val googleServices = "4.4.2"
    val javaVersion = JavaVersion.VERSION_17

    // Android SDK
    const val compileSdk = 36
    const val minSdk = 24
    const val targetSdk = 36

    // AndroidX & Compose
    const val composeBom = "2024.12.01"
    const val composeRuntime = "1.7.0"
    const val composeMaterialIconsExtended = "1.7.6"
    const val activityCompose = "1.13.0"
    const val lifecycleRuntimeCompose = "2.10.0"

    // Kotlin Libraries
    const val coroutines = "1.11.0"
    const val serialization = "1.10.0"

    // Ktor
    const val ktor = "3.5.0"
    const val ktorClientMock = "3.5.0"

    // Koin
    const val koin = "4.2.1"

    // Image loading
    const val coil = "2.7.0"

    // Firebase (BOM)
    const val firebaseBom = "34.13.0"
}
