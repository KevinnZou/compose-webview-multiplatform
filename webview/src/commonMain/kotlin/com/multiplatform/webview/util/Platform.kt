package com.multiplatform.webview.util

/**
 * Created By Kevin Zou On 2023/12/5
 */

/**
 * A class that represents the platform that the code is running on.
 */
internal sealed class Platform {
    /**
     * The Android platform.
     */
    data object Android : Platform()

    /**
     * The Desktop platform.
     */
    data object Desktop : Platform()

    /**
     * The iOS platform.
     */
    data object IOS : Platform()

    /**
     * The Wasm platform.
     */
    data object Wasm : Platform()

    /**
     * Whether the current platform is Android.
     */
    fun isAndroid() = this is Android

    /**
     * Whether the current platform is Desktop.
     */
    fun isDesktop() = this is Desktop

    /**
     * Whether the current platform is iOS.
     */
    fun isIOS() = this is IOS

    /**
     * Whether the current platform is Wasm.
     */
    fun isWasm() = this is Wasm
}

/**
 * Get the current platform.
 */
internal expect fun getPlatform(): Platform
