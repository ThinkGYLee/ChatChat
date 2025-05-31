import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("chat.android.kotlin")
    id("chat.android.firebase.library")
    alias(libs.plugins.jetbrains.kotlin.parcelize)
}

android {
    namespace = "com.gyleedev.util"

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies{
    implementation(libs.google.gson)
    implementation(libs.jsoup)
}