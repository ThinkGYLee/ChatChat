import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("chat.android.application")
    id("chat.android.hilt")
    id("chat.kotlin.hilt")
    id("chat.android.ui")
    id("chat.android.coroutine")
    id("chat.android.firebase.plugin")
    id("chat.android.room")
    alias(libs.plugins.spotless)
    alias(libs.plugins.jetbrains.kotlin.parcelize)
}

android {
    namespace = "com.gyleedev.chatchat"

    defaultConfig {
        applicationId = "com.gyleedev.chatchat"
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    composeCompiler {
        reportsDestination = layout.buildDirectory.dir("compose_compiler")
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":util"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":feature"))
    implementation(libs.google.gson)
    implementation(libs.jsoup)
}
