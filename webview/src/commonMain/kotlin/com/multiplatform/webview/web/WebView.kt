package com.multiplatform.webview.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import com.multiplatform.webview.util.KLogger
import com.multiplatform.webview.util.getPlatform
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.merge

/**
 * Created By Kevin Zou On 2023/8/31
 */

/**
 * Provides a basic WebView composable.
 * This version of the function is provided for backwards compatibility by using the older
 * onCreated and onDispose callbacks and is missing the factory parameter.
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
@Composable
fun WebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    webViewJsBridge: WebViewJsBridge? = null,
    onCreated: () -> Unit = {},
    onDispose: () -> Unit = {},
    platformWebViewParams: PlatformWebViewParams? = null,
) {
    WebView(
        state = state,
        modifier = modifier,
        captureBackPresses = captureBackPresses,
        navigator = navigator,
        webViewJsBridge = webViewJsBridge,
        onCreated = { _ -> onCreated() },
        onDispose = { _ -> onDispose() },
        platformWebViewParams = platformWebViewParams,
    )
}

/**
 * Provides a basic WebView composable.
 *
 * @param state The webview state holder where the Uri to load is defined.
 * @param modifier A compose modifier
 * @param captureBackPresses Set to true to have this Composable capture back presses and navigate
 * the WebView back.
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param onCreated Called when the WebView is first created.
 * @param onDispose Called when the WebView is destroyed.
 * @param factory A function that creates a platform-specific WebView object.
 * @sample sample.BasicWebViewSample
 */
@Composable
fun WebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    webViewJsBridge: WebViewJsBridge? = null,
    onCreated: (NativeWebView) -> Unit = {},
    onDispose: (NativeWebView) -> Unit = {},
    platformWebViewParams: PlatformWebViewParams? = null,
    factory: ((WebViewFactoryParam) -> NativeWebView)? = null,
) {
    val webView = state.webView

    webView?.let { wv ->
        LaunchedEffect(wv, navigator) {
            with(navigator) {
                KLogger.d {
                    "wv.handleNavigationEvents()"
                }
                wv.handleNavigationEvents()
            }
        }

        // Desktop will handle the first load by itself
        if (!getPlatform().isDesktop()) {
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

        // inject the js bridge when the webview is loaded.
        if (webViewJsBridge != null && !getPlatform().isDesktop()) {
            LaunchedEffect(wv, state) {
                val loadingStateFlow =
                    snapshotFlow { state.loadingState }.filter { it is LoadingState.Finished }
                val lastLoadedUrFlow =
                    snapshotFlow { state.lastLoadedUrl }.filter { !it.isNullOrEmpty() }

                // Only inject the js bridge when url is changed and the loading state is finished.
                merge(loadingStateFlow, lastLoadedUrFlow).collect {
                    // double check the loading state to make sure the WebView is loaded.
                    if (state.loadingState is LoadingState.Finished) {
                        wv.injectJsBridge()
                    }
                }
            }
        }
    }

    ActualWebView(
        state = state,
        modifier = modifier,
        captureBackPresses = captureBackPresses,
        navigator = navigator,
        webViewJsBridge = webViewJsBridge,
        onCreated = onCreated,
        onDispose = onDispose,
        platformWebViewParams = platformWebViewParams,
        factory = factory ?: ::defaultWebViewFactory,
    )

    DisposableEffect(Unit) {
        onDispose {
            KLogger.d {
                "WebView DisposableEffect"
            }
            webViewJsBridge?.clear()
        }
    }
}

/**
 * Platform specific parameters given to the WebView factory function. This is a
 * data class containing one or more platform-specific values necessary to
 * create a platform-specific WebView:
 *   - On Android, this contains a `Context` object
 *   - On iOS, this contains a `WKWebViewConfiguration` object created from the
 *     provided WebSettings
 *   - On Desktop, this contains the WebViewState, the KCEFClient, and the
 *     loaded file content (if a file, otherwise, an empty string)
 */
expect class WebViewFactoryParam

/**
 * Platform specific parameters given to the WebView composable function:
 *   - On Android, this contains an optional `AccompanistWebViewClient` and `AccompanistWebChromeClient`
 *   - On iOS, this is currently unused
 *   - On Desktop, this is currently unused
 */
expect class PlatformWebViewParams

/**
 * Platform specific default WebView factory function. This can be called from
 * a custom factory function for any platforms that don't need to be customized.
 */
expect fun defaultWebViewFactory(param: WebViewFactoryParam): NativeWebView

/**
 * Expect API of [WebView] that is implemented in the platform-specific modules.
 */
@Composable
expect fun ActualWebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    webViewJsBridge: WebViewJsBridge? = null,
    onCreated: (NativeWebView) -> Unit = {},
    onDispose: (NativeWebView) -> Unit = {},
    platformWebViewParams: PlatformWebViewParams? = null,
    factory: (WebViewFactoryParam) -> NativeWebView = ::defaultWebViewFactory,
)
