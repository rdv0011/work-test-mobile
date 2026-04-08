import org.gradle.api.JavaVersion

object Versions {
    // Kotlin & Gradle
    const val kotlin = "2.3.0"
    const val agp = "8.9.1"
    const val googleServices = "4.4.2"
    val javaVersion = JavaVersion.VERSION_11

    // Android SDK
    const val compileSdk = 36
    const val minSdk = 24
    const val targetSdk = 36

    // AndroidX & Compose
    const val composeBom = "2024.10.01"
    const val activityCompose = "1.13.0"
    const val lifecycleRuntimeCompose = "2.10.0"

    // Kotlin Libraries
    const val coroutines = "1.10.2"
    const val serialization = "1.10.0"

    // Ktor
    const val ktor = "3.4.2"

    // Koin
    const val koin = "4.2.0"

    // Image loading
    const val coil = "2.7.0"

    // Firebase (BOM)
    const val firebaseBom = "34.11.0"
}
