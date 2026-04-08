import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
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

android {
    namespace = "io.umain.munchies.feature.settings"
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
