import org.jetbrains.kotlin.gradle.dsl.JvmTarget.Companion.fromTarget

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
}

kotlin {
    applyDefaultHierarchyTemplate()

    // androidTarget is automatically created by com.android.kotlin.multiplatform.library
    // Configure it here
    android {
        compileSdk = Versions.compileSdk
        namespace = "io.umain.munchies.designtokens"
    }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget> {
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
