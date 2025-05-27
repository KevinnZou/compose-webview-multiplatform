package com.kevinnzou.sample

import androidx.compose.runtime.Composable

actual fun getPlatformName(): String = "Web"

@Composable
fun MainWebView() = WebViewApp()
