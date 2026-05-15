plugins {
    id("io.umain.munchies.kotlin-multiplatform")
}

kotlin {
    android {
        compileSdk = versions.compileSdk
        namespace = "io.umain.munchies.feature.settings"
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.core)
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
            }
        }
    }
}
