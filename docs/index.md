# Compose Multiplatform WebView

[![Maven Central](https://img.shields.io/maven-central/v/io.github.kevinnzou/compose-webview-multiplatform.svg)](https://search.maven.org/artifact/io.github.kevinnzou/compose-webview-multiplatform)
[![Kotlin](https://img.shields.io/badge/kotlin-v1.9.20-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-v1.5.10-blue)](https://github.com/JetBrains/compose-multiplatform)

![badge-android](http://img.shields.io/badge/platform-android-6EDB8D.svg?style=flat)
![badge-ios](http://img.shields.io/badge/platform-ios-CDCDCD.svg?style=flat)
![badge-desktop](http://img.shields.io/badge/platform-desktop-DB413D.svg?style=flat)

This library provide a WebView widget for JetBrains Compose Multiplatform.

<img src="media/cmm-webview-sample.png" height="500">

> **Note**
> This library is built using the [compose multiplatform library template](https://github.com/KevinnZou/compose-multiplatform-library-template).
> It supports automatic package publishing, documentation, and code style checking.

This library can be considered as the Multiplatform version of [Accompanist Web library](https://github.com/google/accompanist/tree/main/web).
It provides the basic WebView functionalities for JetBrains Compose Multiplatform, which supports loading URLs, HTML, and post data. 

Currently, it supports the platforms of Android, iOS, and Desktop.

## Android

The Android implementation of this library relies on the web module from the [Accompanist Library](https://github.com/google/accompanist/tree/main/web).

**Note:** it has been deprecated in version 0.33.1-alpha. Thus I created a fork of it and used it as the base for this library. 
If you just want to use the WebView in Jetpack Compose, please visit this repo: [https://github.com/KevinnZou/compose-webview](https://github.com/KevinnZou/compose-webview).

## iOS
The iOS implementation of this library relies on [WKWebView](https://developer.apple.com/documentation/webkit/wkwebview).

## Desktop

The Desktop implementation of this library relies on [JavaFX WebView](https://docs.oracle.com/javase/8/javafx/api/javafx/scene/web/WebView.html) for version <= 1.2.0.

Thanks to @DATL4G, starting from version 1.3.0, we switched to [Java CEF Browser](https://github.com/chromiumembedded/java-cef) for better performance.

Starting from version 1.7.0, we switched from Java CEF Browser to [Kotlin CEF Browser](https://github.com/DatL4g/KCEF/tree/master) for more features and better performance.

**Note:** After switching to KCEF, developers need to configure it for the desktop app. Please see the [README.desktop.md](https://github.com/KevinnZou/compose-webview-multiplatform/blob/main/README.desktop.md) for more details.

## License

```
Copyright 2023 Kevin Zou

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```