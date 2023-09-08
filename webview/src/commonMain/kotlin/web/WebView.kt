package web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier

/**
 * Created By Kevin Zou On 2023/8/31
 */
/**
 * A wrapper around the Android View WebView to provide a basic WebView composable.
 *
 * If you require more customisation you are most likely better rolling your own and using this
 * wrapper as an example.
 *
 * The WebView attempts to set the layoutParams based on the Compose modifier passed in. If it
 * is incorrectly sizing, use the layoutParams composable function instead.
 *
 * @param state The webview state holder where the Uri to load is defined.
 * @param modifier A compose modifier
 * @param captureBackPresses Set to true to have this Composable capture back presses and navigate
 * the WebView back.
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param onCreated Called when the WebView is first created, this can be used to set additional
 * settings on the WebView. WebChromeClient and WebViewClient should not be set here as they will be
 * subsequently overwritten after this lambda is called.
 * @param onDispose Called when the WebView is destroyed. Provides a bundle which can be saved
 * if you need to save and restore state in this WebView.
 * @param client Provides access to WebViewClient via subclassing
 * @param chromeClient Provides access to WebChromeClient via subclassing
 * @param factory An optional WebView factory for using a custom subclass of WebView
 * @sample sample.BasicWebViewSample
 */
@Composable
fun WebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    onCreated: () -> Unit = {},
    onDispose: () -> Unit = {},
) {
    val webView = state.webView

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
                        wv.loadUrl(content.url, content.additionalHttpHeaders)
                    }

                    is WebContent.Data -> {
                        wv.loadHtml(
                            content.data,
                            content.baseUrl,
                            content.mimeType,
                            content.encoding,
                            content.historyUrl
                        )
                    }

                    is WebContent.Post -> {
                        wv.postUrl(
                            content.url,
                            content.postData
                        )
                    }

                    is WebContent.NavigatorOnly -> {
                        // NO-OP
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
        onCreated = onCreated,
        onDispose = onDispose,
    )
}

@Composable
expect fun ActualWebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    onCreated: () -> Unit = {},
    onDispose: () -> Unit = {},
)

