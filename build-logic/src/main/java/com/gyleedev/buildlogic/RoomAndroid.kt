package com.gyleedev.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureRoomAndroid() {

    val libs = extensions.libs
    dependencies {
        "implementation"(libs.findLibrary("room.runtime").get())
        "implementation"(libs.findLibrary("room.ktx").get())
        "implementation"(libs.findLibrary("room.paging").get())
        "ksp"(libs.findLibrary("room.compiler").get())
    }
}

internal class RoomAndroidPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            configureRoomAndroid()
        }
    }
}

