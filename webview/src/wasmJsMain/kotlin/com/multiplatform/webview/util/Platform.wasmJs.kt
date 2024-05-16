package com.multiplatform.webview.util

/**
 * Get the current platform.
 */
internal actual fun getPlatform(): Platform {
    return Platform.WasmJs
}

internal actual fun getPlatformVersion(): String {
    // TODO
    return "0.0.0"
}

internal actual fun getPlatformVersionDouble(): Double {
    val systemVersion = getPlatformVersion()
    val components = systemVersion.split(".")
    val major = components.getOrNull(0)?.toDoubleOrNull() ?: 0.0
    val minor = components.getOrNull(1)?.toDoubleOrNull() ?: 0.0
    return major + (minor / 10.0)
}
