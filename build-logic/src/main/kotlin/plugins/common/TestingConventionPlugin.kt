package plugins.common

import org.gradle.api.Plugin
import org.gradle.api.Project
import versions.Versions

class TestingConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.dependencies.add("testImplementation", "io.ktor:ktor-client-mock:${Versions.ktor}")
        target.dependencies.add("testImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}")
        target.dependencies.add("testImplementation", "io.insert-koin:koin-test:${Versions.koin}")
    }
}
