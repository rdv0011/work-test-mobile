plugins {
    id("io.umain.munchies.kotlin-multiplatform")
    id("io.umain.munchies.kotlin-serialization")
}

kotlin {
    android {
        compileSdk = versions.compileSdk
        namespace = "io.umain.munchies.feature.restaurant"
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.core)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions.coroutines}")
                implementation("io.ktor:ktor-client-core:${versions.ktor}")
                implementation("io.insert-koin:koin-core:${versions.koin}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.compose.runtime:runtime:${versions.composeRuntime}")
                implementation("io.insert-koin:koin-android:${versions.koin}")
            }
        }
        val iosMain by getting {
        }
    }
}
