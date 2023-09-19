package com.multiplatform.webview.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.layout.StackPane
import javafx.scene.web.WebView

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

    DisposableEffect(Unit) {
        onDispose {
            currentOnDispose()
        }
    }

    SwingPanel(
        factory = {
            JFXPanel().apply {
                Platform.runLater {
                    val webView = WebView().apply {
                        isVisible = true
                        engine.addLoadListener(state, navigator)
                    }
                    onCreated()
                    val root = StackPane()
                    root.children.add(webView)
                    this.scene = Scene(root)
                    state.webView = DesktopWebView(webView)
                }
            }
        },
        modifier = modifier,
    )
}