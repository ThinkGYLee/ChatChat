package com.gyleedev.buildlogic

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * https://github.com/android/nowinandroid/blob/main/build-logic/convention/src/main/kotlin/com/google/samples/apps/nowinandroid/KotlinAndroid.kt
 */
internal fun Project.configureKotlinAndroid() {
    // Plugins

    with(pluginManager) {
        apply("org.jetbrains.kotlin.android")
        apply("com.android.library")
    }

    // Android settings
    configureGradleScript()

    val libs = extensions.libs
    dependencies {
        dependencies {
            add("implementation", libs.findLibrary("androidx.core.ktx").get())
            add("implementation", libs.findLibrary("junit").get())
            add("implementation", libs.findLibrary("androidx.junit").get())
            add("implementation", libs.findLibrary("androidx.espresso.core").get())
            add("implementation", libs.findLibrary("androidx.lifecycle.runtime.ktx").get())
            add("implementation", libs.findLibrary("material").get())
        }
    }

    configureKotlin()

}

internal fun Project.configureGradleScript() {

    androidExtension.apply {
        compileSdk = 35

        defaultConfig {
            minSdk = 24

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            vectorDrawables {
                useSupportLibrary = true
            }
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
    }
}

internal class KotlinAndroidPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            configureKotlinAndroid()
        }
    }
}
