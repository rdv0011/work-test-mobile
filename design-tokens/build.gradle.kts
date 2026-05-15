plugins {
    id("io.umain.munchies.kotlin-multiplatform")
}

kotlin {
    android {
        compileSdk = versions.compileSdk
        namespace = "io.umain.munchies.designtokens"
    }
    
    sourceSets {
        val commonMain by getting {
        }
    }
}



