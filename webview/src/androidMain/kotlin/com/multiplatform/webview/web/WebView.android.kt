package com.multiplatform.webview.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Android WebView implementation.
 */
@Composable
actual fun ActualWebView(
    state: WebViewState,
    modifier: Modifier,
    captureBackPresses: Boolean,
    navigator: WebViewNavigator,
    permissionHandler: PermissionHandler,
    onCreated: () -> Unit,
    onDispose: () -> Unit,
) {
    AccompanistWebView(
        state,
        modifier,
        captureBackPresses,
        navigator,
        chromeClient = remember { AccompanistWebChromeClient(permissionHandler) },
        onCreated = { _ -> onCreated() },
        onDispose = { _ -> onDispose() },
    )
}
