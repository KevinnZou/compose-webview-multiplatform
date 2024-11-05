package com.multiplatform.webview.web

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
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
    platformWebViewParams: PlatformWebViewParams?,
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
        client = platformWebViewParams?.client ?: remember { AccompanistWebViewClient() },
        chromeClient = platformWebViewParams?.chromeClient ?: remember { AccompanistWebChromeClient() },
        factory = { factory(WebViewFactoryParam(it)) },
    )
}

/** Android WebView factory parameters: a context. */
actual data class WebViewFactoryParam(val context: Context)

/** Default WebView factory for Android. */
actual fun defaultWebViewFactory(param: WebViewFactoryParam) = android.webkit.WebView(param.context)

@Immutable
actual data class PlatformWebViewParams(
    val client: AccompanistWebViewClient? = null,
    val chromeClient: AccompanistWebChromeClient? = null,
)
