package com.multiplatform.webview.web

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import org.w3c.dom.Text

/**
 * Expect API of [WebView] that is implemented in the platform-specific modules.
 */
@Composable
actual fun ActualWebView(
    state: WebViewState,
    modifier: Modifier,
    captureBackPresses: Boolean,
    navigator: WebViewNavigator,
    webViewJsBridge: WebViewJsBridge?,
    onCreated: () -> Unit,
    onDispose: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Text("WebView is not supported on this platform yet.")
    }
}
