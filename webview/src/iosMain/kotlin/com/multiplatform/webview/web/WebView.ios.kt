package com.multiplatform.webview.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

/**
 * iOS WebView implementation.
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
    IOSWebView(
        state = state,
        modifier = modifier,
        captureBackPresses = captureBackPresses,
        navigator = navigator,
        permissionHandler = permissionHandler,
        onCreated = onCreated,
        onDispose = onDispose,
    )
}

/**
 * iOS WebView implementation.
 */
@OptIn(ExperimentalForeignApi::class)
@Composable
fun IOSWebView(
    state: WebViewState,
    modifier: Modifier,
    captureBackPresses: Boolean,
    navigator: WebViewNavigator,
    permissionHandler: PermissionHandler,
    onCreated: () -> Unit,
    onDispose: () -> Unit,
) {
    val observer =
        remember {
            WKWebViewObserver(
                state = state,
                navigator = navigator,
            )
        }
    val navigationDelegate = remember { WKNavigationDelegate(state, navigator) }

    val wkPermissionHandler = remember {
        WKPermissionHandler(permissionHandler)
    }
    UIKitView(
        factory = {
            val config =
                WKWebViewConfiguration().apply {
                    allowsInlineMediaPlayback = true
                }
            WKWebView(
                frame = CGRectZero.readValue(),
                configuration = config,
            ).apply {
                userInteractionEnabled = captureBackPresses
                allowsBackForwardNavigationGestures = captureBackPresses
                customUserAgent = state.webSettings.customUserAgentString
                navigationDelegate = navigationDelegate
                UIDelegate = wkPermissionHandler
                this.addObservers(
                    observer = observer,
                    properties =
                        listOf(
                            "estimatedProgress",
                            "title",
                            "URL",
                            "canGoBack",
                            "canGoForward",
                        ),
                )
                onCreated()
            }.also { state.webView = IOSWebView(it) }
        },
        modifier = modifier,
        onRelease = {
            state.webView = null
            it.removeObservers(
                observer = observer,
                properties =
                    listOf(
                        "estimatedProgress",
                        "title",
                        "URL",
                        "canGoBack",
                        "canGoForward",
                    ),
            )
            it.navigationDelegate = null
            onDispose()
        },
    )
}
