import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("chat.android.kotlin")
    id("chat.android.hilt")
    id("chat.kotlin.hilt")
    id("chat.android.ui")
}

android {
    namespace = "com.gyleedev.feature"

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
    implementation(project(":domain"))
    implementation(project(":core"))
    implementation(project(":util"))
    implementation(libs.androidx.browser)
}
