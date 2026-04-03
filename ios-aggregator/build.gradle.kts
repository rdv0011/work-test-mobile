import org.jetbrains.kotlin.gradle.dsl.JvmTarget.Companion.fromTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    jvmToolchain(Versions.jvmTarget.toInt())
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilerOptions {
            jvmTarget.set(fromTarget(Versions.jvmTarget))
        }
    }

    val xcframeworkName = "shared"
    val xcf = XCFramework(xcframeworkName)

    val iosTargets = listOf(
        iosArm64(),
        iosSimulatorArm64()
    )

    iosTargets.forEach { target ->
        target.binaries.framework {
            baseName = xcframeworkName
            isStatic = true

            export(project(":design-tokens"))
            export(project(":core"))
            export(project(":feature-restaurant"))
            export(project(":feature-settings"))
            export("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
            export("io.insert-koin:koin-core:${Versions.koin}")

            xcf.add(this)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":design-tokens"))
                api(project(":core"))
                api(project(":feature-restaurant"))
                api(project(":feature-settings"))
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "io.umain.munchies.aggregator"
    compileSdk = Versions.compileSdk
    defaultConfig {
        minSdk = Versions.minSdk
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
