import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("chat.android.kotlin")
    id("chat.android.hilt")
    id("chat.android.room")
    id("chat.android.firebase.library")
}

android {
    namespace = "com.gyleedev.data"

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
    implementation(project(":domain"))
}
