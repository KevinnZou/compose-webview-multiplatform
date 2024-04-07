package com.multiplatform.webview.web

import androidx.compose.runtime.Composable
import dev.datlag.kcef.KCEFBrowser

/**
 * Created By Kevin Zou On 2024/4/7
 */
actual typealias PlatformWebView = KCEFBrowser

actual fun getPlatformWebView(context: PlatformContext): PlatformWebView? {
    return null
}

actual class PlatformContext()

@Composable
actual fun getPlatformContext(): PlatformContext = PlatformContext()
