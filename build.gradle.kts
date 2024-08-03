import com.diffplug.gradle.spotless.SpotlessExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.firebase) apply false
}
subprojects {
    plugins.apply("com.diffplug.spotless")

    configure<SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            ktlint()
            indentWithSpaces()
            endWithNewline()
        }
        kotlinGradle {
            target("**/*.gradle.kts")
            ktlint()
            indentWithSpaces()
            endWithNewline()
        }
    }
}