plugins {
    id("com.android.application")
    kotlin("android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

android {
    namespace = "io.umain.munchies.android"
    compileSdk = Versions.compileSdk

    kotlin {
        jvmToolchain(Versions.javaVersion.majorVersion.toInt())
    }

    defaultConfig {
        applicationId = "io.umain.munchies.android"
        minSdk = Versions.minSdk
        targetSdk = Versions.targetSdk
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = Versions.javaVersion
        targetCompatibility = Versions.javaVersion
    }
}

dependencies {
    implementation(project(":design-tokens"))
    implementation(project(":core"))
    implementation(project(":feature-restaurant"))
    implementation(project(":feature-settings"))

    implementation(platform("androidx.compose:compose-bom:${Versions.composeBom}"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.animation:animation")

    implementation("androidx.activity:activity-compose:${Versions.activityCompose}")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${Versions.lifecycleRuntimeCompose}")
    implementation("androidx.tracing:tracing:1.3.0")

    implementation("io.insert-koin:koin-android:${Versions.koin}")
    implementation("io.insert-koin:koin-androidx-compose:${Versions.koin}")
    implementation("io.coil-kt:coil-compose:${Versions.coil}")

    implementation(platform("com.google.firebase:firebase-bom:${Versions.firebaseBom}"))
    implementation("com.google.firebase:firebase-analytics")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${Versions.kotlin}")
    testImplementation("io.insert-koin:koin-test:${Versions.koin}")

    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.activity:activity-compose:${Versions.activityCompose}")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    androidTestImplementation("io.mockk:mockk-android:1.13.16")
    androidTestImplementation("io.ktor:ktor-serialization-gson:${Versions.ktor}")
    androidTestImplementation("androidx.tracing:tracing:1.3.0")
    androidTestImplementation("io.insert-koin:koin-test:${Versions.koin}")
    androidTestImplementation("io.insert-koin:koin-test-junit4:${Versions.koin}")
}
