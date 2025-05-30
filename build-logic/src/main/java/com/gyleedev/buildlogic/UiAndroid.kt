package com.gyleedev.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

internal fun Project.configureUiAndroid() {
    with(plugins) {
        apply("org.jetbrains.kotlin.plugin.compose")
    }

    val libs = extensions.libs
    androidExtension.apply {
        dependencies {
            val bom = libs.findLibrary("androidx.compose.bom").get()
            add("implementation", platform(bom))
            add("androidTestImplementation", platform(bom))
            add("implementation", libs.findLibrary("androidx.activity.compose").get())
            add("implementation", libs.findLibrary("androidx.ui").get())
            add("implementation", libs.findLibrary("androidx.ui.graphics").get())
            add("implementation", libs.findLibrary("androidx.ui.tooling").get())
            add("implementation", libs.findLibrary("androidx.ui.tooling.preview").get())
            add("implementation", libs.findLibrary("androidx.ui.test.manifest").get())
            add("implementation", libs.findLibrary("androidx.ui.test.junit4").get())
            add("implementation", libs.findLibrary("androidx.material").get())
            add("implementation", libs.findLibrary("androidx.material.icons.extended").get())
            add("implementation", libs.findLibrary("androidx.material3").get())
            add("implementation", libs.findLibrary("androidx.lifecycle.compose").get())
            add("implementation", libs.findLibrary("androidx.lifecycle.viewmodel").get())
            add("implementation", libs.findLibrary("androidx.core.ktx").get())
            add("implementation", libs.findLibrary("junit").get())
            add("implementation", libs.findLibrary("androidx.junit").get())
            add("implementation", libs.findLibrary("androidx.espresso.core").get())
            add("implementation", libs.findLibrary("androidx.lifecycle.runtime.ktx").get())
            add("implementation", libs.findLibrary("appcompat").get())
            add("implementation", libs.findLibrary("material").get())
            add("implementation", libs.findLibrary("androidx.core.splash").get())
            add("implementation", libs.findLibrary("landscapist.glide").get())
            add("implementation", libs.findLibrary("landscapist.placeholder").get())
            add("implementation", libs.findLibrary("paging.runtime.ktx").get())
            add("implementation", libs.findLibrary("paging.compose").get())
        }
    }

    extensions.getByType<ComposeCompilerGradlePluginExtension>().apply {
        enableStrongSkippingMode.set(true)
        includeSourceInformation.set(true)
    }
}


internal class UiAndroidPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureUiAndroid()
        }
    }
}
