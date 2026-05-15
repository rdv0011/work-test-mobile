package plugins.common

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.repositories

class CommonConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // Apply dependencies convention to expose versions extension
        target.pluginManager.apply(DependenciesConventionPlugin::class.java)
        
        target.repositories {
            google()
            mavenCentral()
            gradlePluginPortal()
        }
    }
}
