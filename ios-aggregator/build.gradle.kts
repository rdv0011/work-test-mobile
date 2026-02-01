plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

kotlin {
    applyDefaultHierarchyTemplate()
    
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = Versions.jvmTarget
            }
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
            
            // Export all modules for iOS
            export(project(":core"))
            export(project(":ui-components"))
            export(project(":feature-restaurant"))
            
            // Export Koin and Coroutines for iOS interop
            export("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
            export("io.insert-koin:koin-core:${Versions.koin}")
            
            xcf.add(this)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // Aggregate all modules
                api(project(":core"))
                api(project(":ui-components"))
                api(project(":feature-restaurant"))
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
