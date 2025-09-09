import com.diffplug.gradle.spotless.SpotlessExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.firebase) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.crashlytics) apply false
    alias(libs.plugins.jetbrains.kotlin.parcelize) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}

subprojects {
    plugins.apply("com.diffplug.spotless")

    configure<SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            ktlint("1.5.0")
                .setEditorConfigPath("$rootDir/.editorconfig")
            leadingTabsToSpaces(4)
            endWithNewline()
        }
        kotlinGradle {
            target("**/*.gradle.kts")
            ktlint("1.5.0")
                .setEditorConfigPath("$rootDir/.editorconfig")
            leadingTabsToSpaces(4)
            endWithNewline()
        }
    }
}

