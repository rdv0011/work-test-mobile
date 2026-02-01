rootProject.name = "MunchiesApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

include(":androidApp")
include(":design-tokens")
include(":core")
include(":ui-components")
include(":feature-restaurant")
include(":ios-aggregator")
