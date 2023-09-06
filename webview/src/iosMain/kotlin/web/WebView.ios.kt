package web

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSError
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.darwin.NSObject

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
        onCreated = onCreated,
        onDispose = onDispose,
    )
}

@OptIn(ExperimentalForeignApi::class)
@Suppress("CONFLICTING_OVERLOADS")
@Composable
fun IOSWebView(
    state: WebViewState,
    modifier: Modifier,
    captureBackPresses: Boolean,
    onCreated: () -> Unit,
    onDispose: () -> Unit,
) {
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
                navigationDelegate = object : NSObject(), WKNavigationDelegateProtocol {
                    override fun webView(
                        webView: WKWebView,
                        didStartProvisionalNavigation: WKNavigation?
                    ) {
                        state.loadingState = LoadingState.Loading(0f)
                        state.lastLoadedUrl = webView.URL.toString()
                        state.errorsForCurrentRequest.clear()
                        println("didStartProvisionalNavigation")
                    }

                    override fun webView(
                        webView: WKWebView,
                        didCommitNavigation: WKNavigation?
                    ) {
                        state.loadingState =
                            LoadingState.Loading(webView.estimatedProgress.toFloat())
                        println("didCommitNavigation")
                    }

                    override fun webView(
                        webView: WKWebView,
                        didFinishNavigation: WKNavigation?
                    ) {
                        state.pageTitle = webView.title
                        state.lastLoadedUrl = webView.URL.toString()
                        state.loadingState = LoadingState.Finished
                        println("didFinishNavigation")
                    }

                    override fun webView(
                        webView: WKWebView,
                        didFailProvisionalNavigation: WKNavigation?,
                        withError: NSError
                    ) {
                        state.errorsForCurrentRequest.add(
                            WebViewError(
                                withError.code.toInt(),
                                withError.localizedDescription
                            )
                        )
                        println("didFailNavigation")
                    }
                }
            }.also { state.webView = IOSWebView(it) }
        },
        modifier = modifier,
        onRelease = {
            state.webView = null
            onDispose()
        }
    )
}

