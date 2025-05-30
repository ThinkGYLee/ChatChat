plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

group = "com.gyleedev.buildlogic"


dependencies {
    // versionCatalogs에 관한 정보는 하단의 git 링크를 통해 확인해주세요.
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.compose.compiler.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("kotlinHilt") {
            id = "chat.kotlin.hilt"
            implementationClass = "com.gyleedev.buildlogic.HiltKotlinPlugin"
        }
        register("androidHilt") {
            id = "chat.android.hilt"
            implementationClass = "com.gyleedev.buildlogic.HiltAndroidPlugin"
        }
        register("kotlinAndroid") {
            id = "chat.android.kotlin"
            implementationClass = "com.gyleedev.buildlogic.KotlinAndroidPlugin"
        }
        register("composeAndroid") {
            id = "chat.android.compose"
            implementationClass = "com.gyleedev.buildlogic.ComposeAndroidPlugin"
        }
        register("coroutineAndroid") {
            id = "chat.android.coroutine"
            implementationClass = "com.gyleedev.buildlogic.CoroutineAndroidPlugin"
        }
    }
}