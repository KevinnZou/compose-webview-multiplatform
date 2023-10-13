package com.multiplatform.webview.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cef.CefClient
import org.cef.browser.CefBrowser
import java.util.logging.Logger

/**
 * Desktop WebView implementation.
 */
@Composable
actual fun ActualWebView(
    state: WebViewState,
    modifier: Modifier,
    captureBackPresses: Boolean,
    navigator: WebViewNavigator,
    onCreated: () -> Unit,
    onDispose: () -> Unit
) {
    DesktopWebView(
        state,
        modifier,
        navigator,
        onCreated = onCreated,
        onDispose = onDispose
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
    onCreated: () -> Unit,
    onDispose: () -> Unit
) {
    val currentOnDispose by rememberUpdatedState(onDispose)
    val client by produceState<CefClient?>(null) {
        value = withContext(Dispatchers.IO) {
            runCatching { Cef.newClient() }.getOrNull()
        }
    }
    val browser: CefBrowser? = remember(client, state.webSettings.desktopWebSettings) {
        val url = when (val current = state.content) {
            is WebContent.Url -> current.url
            is WebContent.Data -> current.data.toDataUri()
            is WebContent.File -> "<html></html>".toDataUri()
            else -> "about:blank"
        }
        co.touchlab.kermit.Logger.i {
            "Create Browser: $url"
        }

        client?.createBrowser(
            url,
            state.webSettings.desktopWebSettings.offScreenRendering,
            state.webSettings.desktopWebSettings.transparent,
            createModifiedRequestContext(state.webSettings)
        )
    }

    browser?.let {
        state.webView = DesktopWebView(it)

        SwingPanel(
            factory = {
                browser.apply {
                    addDisplayHandler(state)
                    addLoadListener(state, navigator)
                }
                onCreated()
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