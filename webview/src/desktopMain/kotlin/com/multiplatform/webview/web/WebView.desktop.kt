package com.multiplatform.webview.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import dev.datlag.kcef.KCEF
import dev.datlag.kcef.KCEFBrowser
import dev.datlag.kcef.KCEFClient
import org.cef.browser.CefRendering
import org.cef.browser.CefRequestContext

/**
 * Desktop WebView implementation.
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
    DesktopWebView(
        state,
        modifier,
        navigator,
        webViewJsBridge,
        onCreated = onCreated,
        onDispose = onDispose,
        factory = factory,
    )
}

/** Desktop WebView factory parameters: web view state, client, and possible file content. */
actual class WebViewFactoryParam(
    val state: WebViewState,
    val client: KCEFClient,
    val fileContent: String,
) {
    inline val webSettings get() = state.webSettings
    inline val rendering: CefRendering get() =
        if (webSettings.desktopWebSettings.offScreenRendering) {
            CefRendering.OFFSCREEN
        } else {
            CefRendering.DEFAULT
        }
    inline val transparent: Boolean get() = webSettings.desktopWebSettings.transparent
    val requestContext: CefRequestContext get() = createModifiedRequestContext(webSettings)
}

actual class PlatformWebViewParams

/** Default WebView factory for Desktop. */
actual fun defaultWebViewFactory(param: WebViewFactoryParam): NativeWebView =
    when (val content = param.state.content) {
        is WebContent.Url ->
            param.client.createBrowser(
                content.url,
                param.rendering,
                param.transparent,
                param.requestContext,
            )
        is WebContent.Data ->
            param.client.createBrowser(
                KCEFBrowser.BLANK_URI,
                param.rendering,
                param.transparent,
            )
        is WebContent.File -> {
            param.client.createBrowser(
                KCEFBrowser.BLANK_URI,
                param.rendering,
                param.transparent,
                param.requestContext,
            )
        }
        else ->
            param.client.createBrowser(
                KCEFBrowser.BLANK_URI,
                param.rendering,
                param.transparent,
                param.requestContext,
            )
    }

/**
 * Desktop WebView implementation.
 */
@Composable
fun DesktopWebView(
    state: WebViewState,
    modifier: Modifier,
    navigator: WebViewNavigator,
    webViewJsBridge: WebViewJsBridge?,
    onCreated: (NativeWebView) -> Unit,
    onDispose: (NativeWebView) -> Unit,
    factory: (WebViewFactoryParam) -> NativeWebView,
) {
    val currentOnDispose by rememberUpdatedState(onDispose)
    val client =
        remember(state.webSettings.desktopWebSettings.disablePopupWindows) {
            KCEF.newClientOrNullBlocking()?.also {
                if (state.webSettings.desktopWebSettings.disablePopupWindows) {
                    it.addLifeSpanHandler(DisablePopupWindowsLifeSpanHandler())
                } else {
                    if (it.getLifeSpanHandler() is DisablePopupWindowsLifeSpanHandler) {
                        it.removeLifeSpanHandler()
                    }
                }
            }
        }

    val scope = rememberCoroutineScope()
    val browser: KCEFBrowser? =
        remember(client, state.webSettings, state.content) {
            client?.let { factory(WebViewFactoryParam(state, client, "")) }
        }

    val desktopWebView: DesktopWebView? =
        remember(browser, state.content) {
            browser?.let {
                DesktopWebView(browser, scope, webViewJsBridge)
            }
        }

    LaunchedEffect(desktopWebView) {
        desktopWebView?.let { webView ->
            state.webView = webView
            webViewJsBridge?.webView = webView
        }
    }

    browser?.let {
        SwingPanel(
            factory = {
                onCreated(it)
                browser.apply {
                    addDisplayHandler(state)
                    addLoadListener(state, navigator)
                    addRequestHandler(state, navigator)
                }
                browser.uiComponent
            },
            modifier = modifier,
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            client?.dispose()
            browser?.let { currentOnDispose(it) }
        }
    }
}
