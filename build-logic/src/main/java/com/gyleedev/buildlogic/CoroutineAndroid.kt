package com.gyleedev.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureCoroutineAndroid() {
    val libs = extensions.libs
    configureCoroutineKotlin()
    dependencies {
        "implementation"(libs.findLibrary("coroutines.android").get())
    }
}

internal fun Project.configureCoroutineKotlin() {
    val libs = extensions.libs
    dependencies {
        "implementation"(libs.findLibrary("coroutines.core").get())
        "testImplementation"(libs.findLibrary("coroutines.test").get())
    }
}

internal class CoroutineAndroidPlugin(): Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureCoroutineAndroid()
        }
    }
}
