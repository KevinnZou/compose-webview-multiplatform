package com.multiplatform.webview.util

/**
 * Created By Kevin Zou On 2023/12/5
 */
internal sealed class Platform {
    data object Android : Platform()

    data object Desktop : Platform()

    data object IOS : Platform()

    fun isAndroid() = this is Android

    fun isDesktop() = this is Desktop

    fun isIOS() = this is IOS
}

internal expect fun getPlatform(): Platform
