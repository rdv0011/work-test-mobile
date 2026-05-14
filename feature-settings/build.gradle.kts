import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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
        namespace = "io.umain.munchies.feature.settings"
    }

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget> {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(Versions.javaVersion.majorVersion))
        }
    }

    val iosTargets = listOf(
        iosArm64(),
        iosSimulatorArm64()
    )

    iosTargets.forEach { target ->
        target.binaries.framework {
            baseName = "featureSettings"
            isStatic = true
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":core"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}
