import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("chat.android.kotlin")
    id("chat.android.hilt")
    id("chat.android.room")
    alias(libs.plugins.jetbrains.kotlin.parcelize)
}

android {
    namespace = "com.gyleedev.domain"

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.google.gson)
}
