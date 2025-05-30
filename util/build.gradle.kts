import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("chat.android.kotlin")
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