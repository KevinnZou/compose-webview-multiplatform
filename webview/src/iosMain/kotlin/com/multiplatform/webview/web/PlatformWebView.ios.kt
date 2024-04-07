package com.multiplatform.webview.web

import androidx.compose.runtime.Composable
import androidx.compose.ui.interop.LocalUIViewController
import platform.UIKit.UIViewController
import platform.WebKit.WKWebView

/**
 * Created By Kevin Zou On 2024/4/7
 */
actual class PlatformWebView(val iosWebView: WKWebView)

actual fun getPlatformWebView(context: PlatformContext): PlatformWebView? {
    return PlatformWebView(WKWebView())
}

actual class PlatformContext(val iosController: UIViewController)

@Composable
actual fun getPlatformContext(): PlatformContext = PlatformContext(LocalUIViewController.current)
