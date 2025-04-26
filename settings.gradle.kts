rootProject.name = "compose-webview-multiplatform"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://packages.jetbrains.team/maven/p/kpm/public/")
        maven("https://jitpack.io")
        maven("https://jogamp.org/deployment/maven")
    }
}

include(":webview")
include(":sample:androidApp")
include(":sample:desktopApp")
include(":sample:shared")
