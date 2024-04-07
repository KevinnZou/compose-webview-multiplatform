package com.multiplatform.webview.web

import android.content.Context
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Created By Kevin Zou On 2024/4/7
 */
actual typealias PlatformWebView = WebView

actual fun getPlatformWebView(context: PlatformContext): PlatformWebView? {
    return WebView(context.androidContext)
}

actual class PlatformContext(val androidContext: Context)

@Composable
actual fun getPlatformContext(): PlatformContext = LocalContext.current.let { PlatformContext(it) }
