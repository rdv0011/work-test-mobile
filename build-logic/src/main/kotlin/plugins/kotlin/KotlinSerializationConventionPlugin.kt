package plugins.kotlin

import org.gradle.api.Plugin
import org.gradle.api.Project
import versions.Versions

class KotlinSerializationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
        
        target.afterEvaluate {
            target.dependencies.add("commonMainImplementation", "org.jetbrains.kotlinx:kotlinx-serialization-json:${Versions.serialization}")
        }
    }
}

