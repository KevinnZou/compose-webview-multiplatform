package com.multiplatform.webview.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.ui.interop.LocalUIViewController
import platform.UIKit.UIViewController
import platform.WebKit.WKWebView

/**
 * Created By Kevin Zou On 2024/4/7
 */
actual typealias PlatformWebView = WKWebView

actual fun getPlatformWebView(context: PlatformContext): PlatformWebView? {
    return WKWebView()
}

actual class PlatformContext(val iosController: ProvidableCompositionLocal<UIViewController>)

@Composable
actual fun getPlatformContext(): PlatformContext = PlatformContext(LocalUIViewController)
