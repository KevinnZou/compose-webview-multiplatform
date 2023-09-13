package web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@Composable
actual fun ActualWebView(
    state: WebViewState,
    modifier: Modifier,
    captureBackPresses: Boolean,
    navigator: WebViewNavigator,
    onCreated: () -> Unit,
    onDispose: () -> Unit,
) {
    IOSWebView(
        state = state,
        modifier = modifier,
        captureBackPresses = captureBackPresses,
        navigator = navigator,
        onCreated = onCreated,
        onDispose = onDispose,
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
fun IOSWebView(
    state: WebViewState,
    modifier: Modifier,
    captureBackPresses: Boolean,
    navigator: WebViewNavigator,
    onCreated: () -> Unit,
    onDispose: () -> Unit,
) {
    val observer = remember {
        WKWebViewObserver(
            state = state,
            navigator = navigator
        )
    }
    UIKitView(
        factory = {
            val config = WKWebViewConfiguration().apply {
                allowsInlineMediaPlayback = true
            }
            WKWebView(
                frame = CGRectZero.readValue(),
                configuration = config
            ).apply {
                onCreated()
                userInteractionEnabled = captureBackPresses
                allowsBackForwardNavigationGestures = captureBackPresses
                this.addObservers(
                    observer = observer,
                    properties = listOf(
                        "estimatedProgress",
                        "title",
                        "URL",
                        "canGoBack",
                        "canGoForward"
                    )
                )
                this.navigationDelegate = WKNavigationDelegate(state, navigator)
            }.also { state.webView = IOSWebView(it) }
        },
        modifier = modifier,
        onRelease = {
            state.webView = null
            it.removeObservers(
                observer = observer,
                properties = listOf(
                    "estimatedProgress",
                    "title",
                    "URL",
                    "canGoBack",
                    "canGoForward"
                )
            )
            it.navigationDelegate = null
            onDispose()
        }
    )
}