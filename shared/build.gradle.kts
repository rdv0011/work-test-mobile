plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
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
            export("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
            export("io.insert-koin:koin-core:${Versions.koin}")
            xcf.add(this)
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.serialization}")
                implementation("io.ktor:ktor-client-core:${Versions.ktor}")
                implementation("io.ktor:ktor-client-content-negotiation:${Versions.ktor}")
                implementation("io.ktor:ktor-serialization-kotlinx-json:${Versions.ktor}")
                implementation("io.ktor:ktor-client-mock:${Versions.ktor}")
                api("io.insert-koin:koin-core:${Versions.koin}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("io.ktor:ktor-client-mock:${Versions.ktor}")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-android:${Versions.ktor}")
            }
        }
        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:${Versions.ktor}")
            }
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "io.umain.munchies.shared"
    compileSdk = Versions.compileSdk
    defaultConfig {
        minSdk = Versions.minSdk
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    sourceSets {
        getByName("main") {
            res.srcDirs("src/androidMain/res")
        }
    }
}
