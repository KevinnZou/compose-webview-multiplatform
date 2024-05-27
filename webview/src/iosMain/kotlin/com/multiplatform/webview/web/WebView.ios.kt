package com.multiplatform.webview.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import com.multiplatform.webview.util.toUIColor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.setValue
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.javaScriptEnabled

/**
 * iOS WebView implementation.
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
    IOSWebView(
        state = state,
        modifier = modifier,
        captureBackPresses = captureBackPresses,
        navigator = navigator,
        webViewJsBridge = webViewJsBridge,
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
    webViewJsBridge: WebViewJsBridge?,
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
    val scope = rememberCoroutineScope()

    UIKitView(
        factory = {
            val config =
                WKWebViewConfiguration().apply {
                    allowsInlineMediaPlayback = true
                    defaultWebpagePreferences.allowsContentJavaScript =
                        state.webSettings.isJavaScriptEnabled
                    preferences.apply {
                        setValue(
                            state.webSettings.allowFileAccessFromFileURLs,
                            forKey = "allowFileAccessFromFileURLs",
                        )
                        javaScriptEnabled = state.webSettings.isJavaScriptEnabled
                    }
                    setValue(
                        state.webSettings.allowUniversalAccessFromFileURLs,
                        forKey = "allowUniversalAccessFromFileURLs",
                    )
                }
            WKWebView(
                frame = CGRectZero.readValue(),
                configuration = config,
            ).apply {
                onCreated()
                state.viewState?.let {
                    this.interactionState = it
                }
                allowsBackForwardNavigationGestures = captureBackPresses
                customUserAgent = state.webSettings.customUserAgentString
                this.addProgressObservers(
                    observer = observer,
                )
                this.navigationDelegate = navigationDelegate

                state.webSettings.let {
                    val backgroundColor =
                        (it.iOSWebSettings.backgroundColor ?: it.backgroundColor).toUIColor()
                    val scrollViewColor =
                        (
                            it.iOSWebSettings.underPageBackgroundColor
                                ?: it.backgroundColor
                         ).toUIColor()
                    setOpaque(it.iOSWebSettings.opaque)
                    if (!it.iOSWebSettings.opaque) {
                        setBackgroundColor(backgroundColor)
                        scrollView.setBackgroundColor(scrollViewColor)
                    }
                    scrollView.pinchGestureRecognizer?.enabled = it.supportZoom
                }
                state.webSettings.iOSWebSettings.let {
                    with(scrollView) {
                        bounces = it.bounces
                        scrollEnabled = it.scrollEnabled
                        showsHorizontalScrollIndicator = it.showHorizontalScrollIndicator
                        showsVerticalScrollIndicator = it.showVerticalScrollIndicator
                    }
                }
            }.also {
                val iosWebView = IOSWebView(it, scope, webViewJsBridge)
                state.webView = iosWebView
                webViewJsBridge?.webView = iosWebView
            }
        },
        modifier = modifier,
        onRelease = {
            state.webView = null
            it.removeProgressObservers(
                observer = observer,
            )
            it.navigationDelegate = null
            onDispose()
        },
    )
}
