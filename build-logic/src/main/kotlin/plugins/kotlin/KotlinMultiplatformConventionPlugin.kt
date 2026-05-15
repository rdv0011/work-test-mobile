package plugins.kotlin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import versions.Versions
import plugins.common.DependenciesConventionPlugin

class KotlinMultiplatformConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.pluginManager.apply(DependenciesConventionPlugin::class.java)
        target.pluginManager.apply("kotlin-multiplatform")
        target.pluginManager.apply("com.android.kotlin.multiplatform.library")
        
        target.extensions.configure<KotlinMultiplatformExtension> {
            applyDefaultHierarchyTemplate()
            
            targets.withType(org.jetbrains.kotlin.gradle.plugin.mpp.KotlinAndroidTarget::class.java) {
                compilations.all {
                    compileTaskProvider.configure {
                        compilerOptions {
                            jvmTarget.set(JvmTarget.fromTarget(Versions.javaVersion.majorVersion))
                        }
                    }
                }
            }
            
            iosArm64()
            iosSimulatorArm64()
        }
    }
}
