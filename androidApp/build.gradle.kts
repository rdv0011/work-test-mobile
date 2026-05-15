plugins {
    id("io.umain.munchies.versions-only")
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

android {
    namespace = "io.umain.munchies.android"
    compileSdk = versions.compileSdk

    defaultConfig {
        applicationId = "io.umain.munchies.android"
        minSdk = versions.minSdk
        targetSdk = versions.targetSdk
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
        sourceCompatibility = versions.javaVersion
        targetCompatibility = versions.javaVersion
    }
}

dependencies {
    implementation(project(":design-tokens"))
    implementation(project(":core"))
    implementation(project(":feature-restaurant"))
    implementation(project(":feature-settings"))

    implementation(platform("androidx.compose:compose-bom:${versions.composeBom}"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.core:core-ktx:1.18.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core:${versions.composeMaterialIconsExtended}")
    implementation("androidx.compose.animation:animation")

    implementation("androidx.activity:activity-compose:${versions.activityCompose}")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:${versions.lifecycleRuntimeCompose}")
    implementation("androidx.tracing:tracing:1.3.0")

    implementation("io.insert-koin:koin-android:${versions.koin}")
    implementation("io.insert-koin:koin-androidx-compose:${versions.koin}")
    implementation("io.coil-kt:coil-compose:${versions.coil}")

    implementation(platform("com.google.firebase:firebase-bom:${versions.firebaseBom}"))
    implementation("com.google.firebase:firebase-analytics")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${versions.kotlin}")
    testImplementation("io.insert-koin:koin-test:${versions.koin}")

    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.activity:activity-compose:${versions.activityCompose}")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
    androidTestImplementation("io.mockk:mockk-android:1.13.16")
    androidTestImplementation("io.ktor:ktor-serialization-gson:${versions.ktor}")
    androidTestImplementation("androidx.tracing:tracing:1.3.0")
    androidTestImplementation("io.insert-koin:koin-test:${versions.koin}")
    androidTestImplementation("io.insert-koin:koin-test-junit4:${versions.koin}")
}

