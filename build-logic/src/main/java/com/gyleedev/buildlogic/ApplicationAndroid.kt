package com.gyleedev.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * https://github.com/android/nowinandroid/blob/main/build-logic/convention/src/main/kotlin/com/google/samples/apps/nowinandroid/KotlinAndroid.kt
 */
internal fun Project.configureApplicationAndroid() {
    // Plugins

    with(pluginManager) {
        apply("org.jetbrains.kotlin.android")
        apply("com.android.application")
    }
    val libs = extensions.libs
    // Android settings
    androidExtension.apply {

        buildFeatures {
            compose = true
        }

        dependencies {
            add("implementation", libs.findLibrary("androidx.core.ktx").get())
            add("implementation", libs.findLibrary("junit").get())
            add("implementation", libs.findLibrary("androidx.junit").get())
            add("implementation", libs.findLibrary("androidx.espresso.core").get())
            add("implementation", libs.findLibrary("androidx.lifecycle.runtime.ktx").get())
            add("implementation", libs.findLibrary("material").get())
        }
    }
    configureGradleScript()

    configureKotlin()

}

internal fun Project.configureKotlin() {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            // Treat all Kotlin warnings as errors (disabled by default)
            // Override by setting warningsAsErrors=true in your ~/.gradle/gradle.properties
            val warningsAsErrors: String? by project
            allWarningsAsErrors.set(warningsAsErrors.toBoolean())
            freeCompilerArgs.set(
                freeCompilerArgs.get() + listOf(
                    "-opt-in=kotlin.RequiresOptIn",
                )
            )
        }
    }
}

internal class ApplicationAndroidPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            configureApplicationAndroid()
        }
    }
}
