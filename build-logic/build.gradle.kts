plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.0")
    implementation("com.android.tools.build:gradle:9.0.0")
    implementation("com.google.gms:google-services:4.4.2")
    implementation("org.jetbrains.kotlin.plugin.compose:org.jetbrains.kotlin.plugin.compose.gradle.plugin:2.3.0")
    implementation("org.jetbrains.kotlin.plugin.serialization:org.jetbrains.kotlin.plugin.serialization.gradle.plugin:2.3.0")
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

gradlePlugin {
    plugins {
        register("commonConventions") {
            id = "io.umain.munchies.common-conventions"
            implementationClass = "plugins.common.CommonConventionPlugin"
        }
        register("versionsOnlyConventions") {
            id = "io.umain.munchies.versions-only"
            implementationClass = "plugins.common.VersionsOnlyConventionPlugin"
        }
        register("dependenciesConventions") {
            id = "io.umain.munchies.dependencies-conventions"
            implementationClass = "plugins.common.DependenciesConventionPlugin"
        }
        register("testingConventions") {
            id = "io.umain.munchies.testing-conventions"
            implementationClass = "plugins.common.TestingConventionPlugin"
        }
        register("kotlinJvmConventions") {
            id = "io.umain.munchies.kotlin-jvm"
            implementationClass = "plugins.kotlin.KotlinJvmConventionPlugin"
        }
        register("kotlinMultiplatformConventions") {
            id = "io.umain.munchies.kotlin-multiplatform"
            implementationClass = "plugins.kotlin.KotlinMultiplatformConventionPlugin"
        }
        register("kotlinIosConventions") {
            id = "io.umain.munchies.kotlin-ios"
            implementationClass = "plugins.kotlin.KotlinIosConventionPlugin"
        }
        register("kotlinSerializationConventions") {
            id = "io.umain.munchies.kotlin-serialization"
            implementationClass = "plugins.kotlin.KotlinSerializationConventionPlugin"
        }
        register("rootConventions") {
            id = "io.umain.munchies.root"
            implementationClass = "plugins.root.RootConventionPlugin"
        }
    }
}

