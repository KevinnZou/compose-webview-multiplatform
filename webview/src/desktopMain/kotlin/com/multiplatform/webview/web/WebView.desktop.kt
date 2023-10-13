package com.multiplatform.webview.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import dev.datlag.kcef.KCEF
import dev.datlag.kcef.KCEFBrowser
import dev.datlag.kcef.KCEFClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cef.browser.CefRendering


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

@Composable
fun DesktopWebView(
    state: WebViewState,
    modifier: Modifier,
    navigator: WebViewNavigator,
    onCreated: () -> Unit,
    onDispose: () -> Unit
) {
    val currentOnDispose by rememberUpdatedState(onDispose)
    val client by produceState<KCEFClient?>(null) {
        value = withContext(Dispatchers.IO) {
            KCEF.newClientOrNull()
        }
    }
    val browser: KCEFBrowser? = remember(client, state.webSettings.desktopWebSettings) {
        val rendering = if (state.webSettings.desktopWebSettings.offScreenRendering) {
            CefRendering.OFFSCREEN
        } else {
            CefRendering.DEFAULT
        }

        when (val current = state.content) {
            is WebContent.Url -> client?.createBrowser(
                current.url,
                rendering,
                state.webSettings.desktopWebSettings.transparent,
                createModifiedRequestContext(state.webSettings)
            )
            is WebContent.Data -> client?.createBrowserWithHtml(
                current.data,
                current.baseUrl ?: KCEFBrowser.BLANK_URI,
                rendering,
                state.webSettings.desktopWebSettings.transparent,
                createModifiedRequestContext(state.webSettings)
            )
            else -> {
                client?.createBrowser(
                    KCEFBrowser.BLANK_URI,
                    rendering,
                    state.webSettings.desktopWebSettings.transparent,
                    createModifiedRequestContext(state.webSettings)
                )
            }
        }
    }

    browser?.let {
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