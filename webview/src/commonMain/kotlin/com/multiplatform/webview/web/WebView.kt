package com.multiplatform.webview.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.multiplatform.webview.jsbridge.JsBridge
import com.multiplatform.webview.util.Platform
import com.multiplatform.webview.util.getPlatform
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Created By Kevin Zou On 2023/8/31
 */

/**
 *
 * A wrapper around the Android View WebView to provide a basic WebView composable.
 *
 * @param state The webview state holder where the Uri to load is defined.
 * @param modifier A compose modifier
 * @param captureBackPresses Set to true to have this Composable capture back presses and navigate
 * the WebView back.
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param onCreated Called when the WebView is first created.
 * @param onDispose Called when the WebView is destroyed.
 * @sample sample.BasicWebViewSample
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun WebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    jsBridge: JsBridge? = null,
    onCreated: () -> Unit = {},
    onDispose: () -> Unit = {},
) {
    val webView = state.webView
    var isJsBridgeInjected by remember {
        mutableStateOf(false)
    }

    webView?.let { wv ->
        LaunchedEffect(wv, navigator) {
            with(navigator) {
                wv.handleNavigationEvents()
            }
        }

        LaunchedEffect(wv, state) {
            snapshotFlow { state.content }.collect { content ->
                when (content) {
                    is WebContent.Url -> {
                        state.lastLoadedUrl = content.url
                        wv.loadUrl(content.url, content.additionalHttpHeaders)
                    }

                    is WebContent.Data -> {
                        wv.loadHtml(
                            content.data,
                            content.baseUrl,
                            content.mimeType,
                            content.encoding,
                            content.historyUrl,
                        )
                    }

                    is WebContent.File -> {
                        wv.loadHtmlFile(content.fileName)
                    }

                    is WebContent.Post -> {
                        wv.postUrl(
                            content.url,
                            content.postData,
                        )
                    }

                    is WebContent.NavigatorOnly -> {
                        // NO-OP
                    }
                }
            }
        }
    }

    // TODO WorkAround for Desktop not working issue.
    if (jsBridge != null && !isJsBridgeInjected && getPlatform() != Platform.Desktop) {
        LaunchedEffect(state.loadingState, jsBridge) {
            if (state.loadingState is LoadingState.Finished && !isJsBridgeInjected) {
                webView?.injectInitJS()
                isJsBridgeInjected = true
            }
        }
    }

    ActualWebView(
        state = state,
        modifier = modifier,
        captureBackPresses = captureBackPresses,
        navigator = navigator,
        jsBridge = jsBridge,
        onCreated = onCreated,
        onDispose = onDispose,
    )
}

/**
 * Expect API of [WebView] that is implemented in the platform-specific modules.
 */
@Composable
expect fun ActualWebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    jsBridge: JsBridge? = null,
    onCreated: () -> Unit = {},
    onDispose: () -> Unit = {},
)
