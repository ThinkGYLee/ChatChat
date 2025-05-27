plugins {
    `kotlin-dsl`
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
        register("androidHilt") {
            id = "chat.android.hilt"
            implementationClass = "com.gyleedev.build_logic.HiltKotlinPlugin"
        }
    }
}