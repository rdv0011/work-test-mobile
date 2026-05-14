buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.10")
        classpath("org.jetbrains.kotlin.multiplatform:org.jetbrains.kotlin.multiplatform.gradle.plugin:${Versions.kotlin}")
        classpath("com.android.tools.build:gradle:${Versions.agp}")
        classpath("com.google.gms:google-services:${Versions.googleServices}")
        classpath("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:${Versions.kotlin}")
        classpath("org.jetbrains.kotlin.plugin.serialization:org.jetbrains.kotlin.plugin.serialization.gradle.plugin:${Versions.kotlin}")
    }
}

plugins {
    // Moved to buildscript to allow dynamic versions from Versions.kt
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
