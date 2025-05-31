package com.gyleedev.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureFirebaseAndroidPlugin() {
    with(pluginManager) {
        apply("com.google.gms.google-services")
        apply("com.google.firebase.crashlytics")
    }
}

internal fun Project.configureFirebaseAndroidLibrary() {
    val libs = extensions.libs
    androidExtension.apply {
        dependencies {
            val bom = libs.findLibrary("firebase.bom").get()
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


internal class FirebaseAndroidPluginAndLibrary : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureFirebaseAndroidPlugin()
            configureFirebaseAndroidLibrary()
        }
    }
}

internal class FirebaseAndroidLibrary : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            configureFirebaseAndroidLibrary()
        }
    }
}
