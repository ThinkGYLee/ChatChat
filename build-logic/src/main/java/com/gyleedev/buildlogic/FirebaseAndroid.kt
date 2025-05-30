package com.gyleedev.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureFirebaseAndroid() {
    with(plugins) {
        apply("com.google.gms.google-services")
        apply("com.google.firebase.crashlytics")
    }

    val libs = extensions.libs
    androidExtension.apply {
        dependencies {
            val bom = libs.findLibrary("androidx.compose.bom").get()
            add("implementation", platform(bom))
            add("implementation", libs.findLibrary("firebase.bom").get())
            add("implementation", libs.findLibrary("firebase.analystics").get())
            add("implementation", libs.findLibrary("firebase.crashlytics").get())
            add("implementation", libs.findLibrary("firebase.auth").get())
            add("implementation", libs.findLibrary("firebase.services.auth").get())
            add("implementation", libs.findLibrary("firebase.database").get())
            add("implementation", libs.findLibrary("firebase.storage").get())
            add("implementation", libs.findLibrary("firebase.messaging").get())

        }
    }
}


internal class FirebaseAndroidPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureFirebaseAndroid()

        }
    }
}
