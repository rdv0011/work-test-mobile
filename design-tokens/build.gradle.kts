plugins {
    kotlin("multiplatform")
    id("com.android.library")
}

kotlin {
    applyDefaultHierarchyTemplate()
    
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = Versions.jvmTarget
            }
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
    
    defaultConfig {
        minSdk = Versions.minSdk
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
