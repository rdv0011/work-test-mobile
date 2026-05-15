package plugins.kotlin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import plugins.common.DependenciesConventionPlugin

class KotlinIosConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply(DependenciesConventionPlugin::class.java)
        target.pluginManager.apply("kotlin-multiplatform")
        
        target.extensions.configure<KotlinMultiplatformExtension> {
            applyDefaultHierarchyTemplate()
            
            iosArm64()
            iosSimulatorArm64()
        }
    }
}
