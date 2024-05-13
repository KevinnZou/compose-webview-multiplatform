package com.kevinnzou.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "ComposeTarget") { WebViewApp() }
}

actual fun getPlatformName(): String {
    return "JS/WASM"
}
