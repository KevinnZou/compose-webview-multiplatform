package com.multiplatform.webview.util

import android.os.Build

internal actual fun getPlatform(): Platform {
    return Platform.Android
}

internal actual fun getPlatformVersion(): String {
    return Build.VERSION.RELEASE
}

internal actual fun getPlatformVersionDouble(): Double {
    val systemVersion = getPlatformVersion()
    val components = systemVersion.split(".")
    val major = components.getOrNull(0)?.toDoubleOrNull() ?: 0.0
    val minor = components.getOrNull(1)?.toDoubleOrNull() ?: 0.0
    return major + (minor / 10.0)
}
