package com.multiplatform.webview.web

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import compose_webview_multiplatform.webview.generated.resources.Res
import dev.datlag.kcef.KCEF
import dev.datlag.kcef.KCEFBrowser
import org.cef.browser.CefRendering
import org.jetbrains.compose.resources.ExperimentalResourceApi

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
    onCreated: () -> Unit,
    onDispose: () -> Unit,
) {
    DesktopWebView(
        state,
        modifier,
        navigator,
        webViewJsBridge,
        onCreated = onCreated,
        onDispose = onDispose,
    )
}

/**
 * Desktop WebView implementation.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun DesktopWebView(
    state: WebViewState,
    modifier: Modifier,
    navigator: WebViewNavigator,
    webViewJsBridge: WebViewJsBridge?,
    onCreated: () -> Unit,
    onDispose: () -> Unit,
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
    val fileContent by produceState("", state.content) {
        value =
            if (state.content is WebContent.File) {
                val res = Res.readBytes("assets/${(state.content as WebContent.File).fileName}")
                res.decodeToString().trimIndent()
            } else {
                ""
            }
    }

    val browser: KCEFBrowser? =
        remember(
            client,
            state.webSettings.desktopWebSettings.offScreenRendering,
            state.webSettings.desktopWebSettings.transparent,
            state.webSettings,
            fileContent,
        ) {
            val rendering =
                if (state.webSettings.desktopWebSettings.offScreenRendering) {
                    CefRendering.OFFSCREEN
                } else {
                    CefRendering.DEFAULT
                }

            when (val current = state.content) {
                is WebContent.Url ->
                    client?.createBrowser(
                        current.url,
                        rendering,
                        state.webSettings.desktopWebSettings.transparent,
                        createModifiedRequestContext(state.webSettings),
                    )

                is WebContent.Data ->
                    client?.createBrowserWithHtml(
                        current.data,
                        current.baseUrl ?: KCEFBrowser.BLANK_URI,
                        rendering,
                        state.webSettings.desktopWebSettings.transparent,
                    )

                is WebContent.File ->
                    client?.createBrowserWithHtml(
                        fileContent,
                        KCEFBrowser.BLANK_URI,
                        rendering,
                        state.webSettings.desktopWebSettings.transparent,
                    )

                else -> {
                    client?.createBrowser(
                        KCEFBrowser.BLANK_URI,
                        rendering,
                        state.webSettings.desktopWebSettings.transparent,
                        createModifiedRequestContext(state.webSettings),
                    )
                }
            }
        }
    val desktopWebView =
        remember(browser) {
            if (browser != null) {
                DesktopWebView(browser, scope, webViewJsBridge)
            } else {
                null
            }
        }

    browser?.let {
        SwingPanel(
            factory = {
                onCreated()
                state.webView = desktopWebView
                webViewJsBridge?.webView = desktopWebView
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
            currentOnDispose()
        }
    }
}
