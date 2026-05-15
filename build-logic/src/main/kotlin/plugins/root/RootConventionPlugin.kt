package plugins.root

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete

class RootConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.register("clean", Delete::class.java) {
            delete(target.layout.buildDirectory)
        }
    }
}
