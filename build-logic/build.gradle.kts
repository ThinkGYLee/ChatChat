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
        register("uiAndroid") {
            id = "chat.android.ui"
            implementationClass = "com.gyleedev.buildlogic.UiAndroidPlugin"
        }
        register("coroutineAndroid") {
            id = "chat.android.coroutine"
            implementationClass = "com.gyleedev.buildlogic.CoroutineAndroidPlugin"
        }
        register("firebaseAndroidPluginAndLibrary") {
            id = "chat.android.firebase.plugin"
            implementationClass = "com.gyleedev.buildlogic.FirebaseAndroidPluginAndLibrary"
        }
        register("firebaseAndroidLibrary") {
            id = "chat.android.firebase.library"
            implementationClass = "com.gyleedev.buildlogic.FirebaseAndroidLibrary"
        }
        register("applicationAndroid") {
            id = "chat.android.application"
            implementationClass = "com.gyleedev.buildlogic.ApplicationAndroidPlugin"
        }
        register("roomAndroid") {
            id = "chat.android.room"
            implementationClass = "com.gyleedev.buildlogic.RoomAndroidPlugin"
        }
    }
}