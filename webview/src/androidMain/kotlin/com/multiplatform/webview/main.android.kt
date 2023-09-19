package com.multiplatform.webview

import androidx.compose.runtime.Composable

actual fun getPlatformName(): String = "Android"

@Composable
fun MainWebView() = WebViewApp()
