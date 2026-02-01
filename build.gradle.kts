plugins {
    kotlin("multiplatform").version(Versions.kotlin).apply(false)
    kotlin("android").version(Versions.kotlin).apply(false)
    id("com.android.application").version(Versions.androidGradlePlugin).apply(false)
    id("com.android.library").version(Versions.androidGradlePlugin).apply(false)
    kotlin("plugin.serialization").version(Versions.kotlin).apply(false)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
