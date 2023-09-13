# WebView for JetBrains Compose Multiplatform

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kevinnzou/compose-webview-multiplatform.svg)](https://search.maven.org/artifact/io.github.kevinnzou/compose-webview-multiplatform)

This library provides the basic WebView for JetBrains Compose Multiplatform. It supports loading URLs, HTML, and post data. Currently, it supports the platforms of Android and iOS. It will also support the Desktop in the near future.

The Android implementation of this library relies on the web module from the [Accompanist Library](https://github.com/google/accompanist/tree/main/web). However, it has been deprecated in version 0.33.1-alpha. Thus I created a fork of it and used it as the base for this lirbary. If you just want to use the WebView in Jetpack Compose, please visit this repo: https://github.com/KevinnZou/compose-webview.

The iOS implementation of this library relies on [WKWebView](https://developer.apple.com/documentation/webkit/wkwebview). 


## Basic Usage

To use this widget there are two key APIs that are needed: WebView, which provides the layout, and rememberWebViewState(url) which provides some remembered state including the URL to display.

The basic usage is as follows:
```kotlin
val state = rememberWebViewState("https://example.com")

WebView(state)
```
This will display a WebView in your Compose layout that shows the URL provided.

## Download

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kevinnzou/compose-webview-multiplatform.svg)](https://search.maven.org/artifact/io.github.kevinnzou/compose-webview-multiplatform)

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "io.github.kevinnzou:compose-webview-multiplatform:1.0.0"
}
```