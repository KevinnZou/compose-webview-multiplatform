package com.multiplatform.webview.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.multiplatform.webview.util.KLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cef.CefClient
import org.cef.browser.CefBrowser
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource

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
@OptIn(ExperimentalResourceApi::class)
@Composable
fun DesktopWebView(
    state: WebViewState,
    modifier: Modifier,
    navigator: WebViewNavigator,
    onCreated: () -> Unit,
    onDispose: () -> Unit
) {
    val currentOnDispose by rememberUpdatedState(onDispose)
    val fileContent by produceState("", state.content) {
        value = if (state.content is WebContent.File) {
            val res = resource((state.content as WebContent.File).fileName)
            res.readBytes().decodeToString().trimIndent()
        } else {
            ""
        }
    }
    val client by produceState<CefClient?>(null) {
        value = withContext(Dispatchers.IO) {
            runCatching { Cef.newClient() }.getOrNull()
        }
    }
    val browser: CefBrowser? = remember(client, state.webSettings.desktopWebSettings, fileContent) {
        val url = when (val current = state.content) {
            is WebContent.Url -> current.url
            is WebContent.Data -> current.data.toDataUri()
            is WebContent.File -> fileContent.toDataUri()
            else -> "about:blank"
        }
        KLogger.d {
            "Create Browser: $url"
        }

        client?.createBrowser(
            url,
            state.webSettings.desktopWebSettings.offScreenRendering,
            state.webSettings.desktopWebSettings.transparent,
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