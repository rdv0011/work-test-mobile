plugins {
    id("io.umain.munchies.kotlin-multiplatform")
    id("io.umain.munchies.kotlin-serialization")
}

kotlin {
    android {
        compileSdk = versions.compileSdk
        namespace = "io.umain.munchies.core"
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.designTokens)
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions.coroutines}")
                implementation("io.ktor:ktor-client-core:${versions.ktor}")
                implementation("io.ktor:ktor-client-content-negotiation:${versions.ktor}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${versions.ktor}")

                api("io.insert-koin:koin-core:${versions.koin}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.ktor:ktor-client-mock:${versions.ktorClientMock}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${versions.coroutines}")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-android:${versions.ktor}")
                implementation("io.insert-koin:koin-android:${versions.koin}")
                implementation("androidx.datastore:datastore-preferences:1.0.0")
            }
        }
        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:${versions.ktor}")
            }
        }
        val iosTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions.coroutines}")
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}
