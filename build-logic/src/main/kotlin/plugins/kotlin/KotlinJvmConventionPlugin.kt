package plugins.kotlin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import versions.Versions

class KotlinJvmConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("org.jetbrains.kotlin.jvm")
        
        target.extensions.configure(KotlinJvmProjectExtension::class.java) {
            jvmToolchain(Versions.javaVersion.majorVersion.toInt())
        }
    }
}
