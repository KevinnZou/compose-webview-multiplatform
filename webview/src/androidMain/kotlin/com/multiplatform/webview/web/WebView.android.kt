package com.multiplatform.webview.web

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.multiplatform.webview.jsbridge.WebViewJsBridge

/**
 * Android WebView implementation.
 */
@Composable
actual fun ActualWebView(
    state: WebViewState,
    modifier: Modifier,
    captureBackPresses: Boolean,
    navigator: WebViewNavigator,
    webViewJsBridge: WebViewJsBridge?,
    onCreated: (NativeWebView) -> Unit,
    onDispose: (NativeWebView) -> Unit,
    factory: (WebViewFactoryParam) -> NativeWebView,
) {
    AccompanistWebView(
        state,
        modifier,
        captureBackPresses,
        navigator,
        webViewJsBridge,
        onCreated = onCreated,
        onDispose = onDispose,
        factory = { factory(WebViewFactoryParam(it)) },
    )
}

/** Android WebView factory parameters: a context. */
actual data class WebViewFactoryParam(val context: Context)

/** Default WebView factory for Android. */
actual fun defaultWebViewFactory(param: WebViewFactoryParam) = android.webkit.WebView(param.context)
