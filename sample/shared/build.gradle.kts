import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatorm)
    alias(libs.plugins.kotlin.atomicfu)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

@OptIn(org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi::class)
kotlin {
    applyDefaultHierarchyTemplate()

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.components.resources)
            implementation(libs.compose.navigation)
            implementation(libs.material.icons.core)
            implementation(libs.kermit)
            implementation(libs.kotlin.serialization.json)
            implementation(libs.kotlin.atomicfu)
            implementation(libs.kotlin.coroutines.core)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.tabNavigator)

            api(project(":webview"))
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        androidMain.dependencies {
            api(libs.android.activity.compose)
            api(libs.android.appcompat)
            implementation(libs.kotlin.coroutines.android)
        }
        val desktopMain by getting
        desktopMain.dependencies {
            implementation(compose.desktop.common)
            implementation(libs.kotlin.coroutines.swing)
        }
    }
}

android {
    namespace = "com.kevinnzou.sample"
    compileSdk = 36

    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}
