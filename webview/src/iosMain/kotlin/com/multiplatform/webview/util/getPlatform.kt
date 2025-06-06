package com.multiplatform.webview.util

import platform.UIKit.UIDevice

internal actual fun getPlatform(): Platform = Platform.IOS

internal actual fun getPlatformVersion(): String = UIDevice.currentDevice.systemVersion

internal actual fun getPlatformVersionDouble(): Double {
    val systemVersion = getPlatformVersion()
    val components = systemVersion.split(".")
    val major = components.getOrNull(0)?.toDoubleOrNull() ?: 0.0
    val minor = components.getOrNull(1)?.toDoubleOrNull() ?: 0.0
    return major + (minor / 10.0)
}
