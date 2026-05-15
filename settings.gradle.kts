rootProject.name = "MunchiesApp"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

includeBuild("build-logic")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(":androidApp")
include(":design-tokens")
include(":core")
include(":feature-restaurant")
include(":feature-settings")
include(":ios-aggregator")
