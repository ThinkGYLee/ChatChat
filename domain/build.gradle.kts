import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("chat.android.kotlin")
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
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.google.gson)
    implementation(libs.room.paging)
}
