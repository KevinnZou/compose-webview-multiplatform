package com.kevinnzou.sample

import androidx.compose.runtime.Composable

actual fun getPlatformName(): String = "Android"

@Composable
fun MainWebView() = WebViewApp()
