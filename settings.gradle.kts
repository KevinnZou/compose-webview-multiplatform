rootProject.name = "compose-webview-multiplatform"

include(":sample:androidApp")
include(":webview")
include(":sample:desktopApp")
include(":sample:shared")
include(":sample:composeApp")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven { url = uri("https://jitpack.io") }
    }
}
include(":shared")
