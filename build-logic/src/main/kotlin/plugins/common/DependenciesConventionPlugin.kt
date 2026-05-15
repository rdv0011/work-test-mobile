package plugins.common

import org.gradle.api.Plugin
import org.gradle.api.Project
import versions.Versions

class DependenciesConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val versions = Versions
        
        target.extensions.add("versions", versions)
    }
}
