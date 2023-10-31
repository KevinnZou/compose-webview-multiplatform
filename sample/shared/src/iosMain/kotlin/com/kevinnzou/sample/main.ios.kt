package com.kevinnzou.sample

import androidx.compose.ui.window.ComposeUIViewController

actual fun getPlatformName(): String = "iOS"

fun MainViewController() = ComposeUIViewController { WebViewApp() }
