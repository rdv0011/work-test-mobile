plugins {
    id("io.umain.munchies.kotlin-ios")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.core)
                api(projects.featureRestaurant)
                api(projects.featureSettings)
            }
        }
    }
}
