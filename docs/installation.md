# Installation

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kevinnzou/compose-webview-multiplatform.svg)](https://search.maven.org/artifact/io.github.kevinnzou/compose-webview-multiplatform)

You can add this library to your project using Gradle.

## Multiplatform

To add to a multiplatform project, add the dependency to the common source-set:

```kotlin
repositories {
    mavenCentral()
    // Desktop target has to add this repo
    maven("https://jogamp.org/deployment/maven")
}

kotlin {
    sourceSets {
        commonMain {
            dependencies {
              // use api since the desktop app need to access the Cef to initialize it.
              api("io.github.kevinnzou:compose-webview-multiplatform:1.8.4")
            }
        }
    }
}
```

**Note:** 
If you want to use this library in a desktop app, you need to configure the KCEF for it.
Please see the [README.desktop.md](https://github.com/KevinnZou/compose-webview-multiplatform/blob/main/README.desktop.md) 
for detailed instructions.

## Single Platform

For an Android only project, you directly can use my [another library](https://github.com/KevinnZou/compose-webview).
Add the dependency to app level `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.github.KevinnZou:compose-webview:0.33.3")
}
```