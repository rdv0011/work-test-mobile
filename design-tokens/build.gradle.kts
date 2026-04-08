import org.jetbrains.kotlin.gradle.dsl.JvmTarget.Companion.fromTarget

plugins {
    kotlin("multiplatform")
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
            baseName = "design_tokens"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // No external dependencies needed for design tokens
            }
        }
    }
}

android {
    namespace = "io.umain.munchies.designtokens"
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
}
