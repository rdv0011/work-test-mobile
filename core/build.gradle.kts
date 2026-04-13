import org.gradle.kotlin.dsl.implementation
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.Companion.fromTarget

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilerOptions {
            jvmTarget.set(fromTarget(Versions.javaVersion.majorVersion))
        }
    }

    val iosTargets = listOf(
        iosArm64(),
        iosSimulatorArm64()
    )

    iosTargets.forEach { target ->
        target.binaries.framework {
            baseName = "core"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.designTokens)
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.serialization}")
                implementation("io.ktor:ktor-client-core:${Versions.ktor}")
                implementation("io.ktor:ktor-client-content-negotiation:${Versions.ktor}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${Versions.ktor}")

                api("io.insert-koin:koin-core:${Versions.koin}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.ktor:ktor-client-mock:${Versions.ktor}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-android:${Versions.ktor}")
                implementation("io.insert-koin:koin-android:${Versions.koin}")
                implementation("androidx.datastore:datastore-preferences:1.0.0")
            }
        }
        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:${Versions.ktor}")
            }
        }
        val androidInstrumentedTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}")
            }
        }
        val iosTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "io.umain.munchies.core"
    compileSdk = Versions.compileSdk

    kotlin {
        jvmToolchain(Versions.javaVersion.majorVersion.toInt())
    }

    defaultConfig {
        minSdk = Versions.minSdk
    }
    compileOptions {
        sourceCompatibility = Versions.javaVersion
        targetCompatibility = Versions.javaVersion
    }
    sourceSets {
        getByName("main") {
            res.srcDirs("src/androidMain/res")
        }
    }
}
