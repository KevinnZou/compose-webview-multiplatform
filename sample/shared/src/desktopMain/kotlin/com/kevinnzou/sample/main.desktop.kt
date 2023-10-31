package com.kevinnzou.sample

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable

actual fun getPlatformName(): String = "Desktop"

@Composable
fun MainWebView() = WebViewApp()

@Preview
@Composable
fun AppPreview() {
    WebViewApp()
}
