package com.multiplatform.webview.web

import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.request.WebRequestInterceptResult
import com.multiplatform.webview.util.KLogger
import com.multiplatform.webview.util.getPlatformVersionDouble
import com.multiplatform.webview.util.notZero
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGPointMake
import platform.Foundation.HTTPMethod
import platform.Foundation.NSError
import platform.Foundation.allHTTPHeaderFields
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationAction
import platform.WebKit.WKNavigationActionPolicy
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.darwin.NSObject

/**
 * Created By Kevin Zou On 2023/9/13
 */

/**
 * Navigation delegate for the WKWebView
 */
@Suppress("CONFLICTING_OVERLOADS")
class WKNavigationDelegate(
    private val state: WebViewState,
    private val navigator: WebViewNavigator,
) : NSObject(), WKNavigationDelegateProtocol {
    private var isRedirect = false

    /**
     * Called when the web view begins to receive web content.
     */
    override fun webView(
        webView: WKWebView,
        didStartProvisionalNavigation: WKNavigation?,
    ) {
        state.loadingState = LoadingState.Loading(0f)
        state.lastLoadedUrl = webView.URL.toString()
        state.errorsForCurrentRequest.clear()
        KLogger.info {
            "didStartProvisionalNavigation"
        }
    }

    /**
     * Called when the web view receives a server redirect.
     */
    override fun webView(
        webView: WKWebView,
        didCommitNavigation: WKNavigation?,
    ) {
        val supportZoom = if (state.webSettings.supportZoom) "yes" else "no"

        @Suppress("ktlint:standard:max-line-length")
        val script =
            "var meta = document.createElement('meta');meta.setAttribute('name', 'viewport');meta.setAttribute('content', 'width=device-width, initial-scale=${state.webSettings.zoomLevel}, maximum-scale=10.0, minimum-scale=0.1,user-scalable=$supportZoom');document.getElementsByTagName('head')[0].appendChild(meta);"
        webView.evaluateJavaScript(script) { _, _ -> }
        KLogger.info { "didCommitNavigation" }
    }

    /**
     * Called when the web view finishes loading.
     */
    @OptIn(ExperimentalForeignApi::class)
    override fun webView(
        webView: WKWebView,
        didFinishNavigation: WKNavigation?,
    ) {
        state.pageTitle = webView.title
        state.lastLoadedUrl = webView.URL.toString()
        state.loadingState = LoadingState.Finished
        navigator.canGoBack = webView.canGoBack
        navigator.canGoForward = webView.canGoForward
        // Restore scroll position on iOS 14 and below
        if (getPlatformVersionDouble() < 15.0) {
            if (state.scrollOffset.notZero()) {
                webView.scrollView.setContentOffset(
                    CGPointMake(
                        x = state.scrollOffset.first.toDouble(),
                        y = state.scrollOffset.second.toDouble(),
                    ),
                    true,
                )
            }
        }
        KLogger.info { "didFinishNavigation ${state.lastLoadedUrl}" }
    }

    /**
     * Called when the web view fails to load content.
     */
    override fun webView(
        webView: WKWebView,
        didFailProvisionalNavigation: WKNavigation?,
        withError: NSError,
    ) {
        KLogger.e {
            "WebView Loading Failed with error: ${withError.localizedDescription}"
        }
        state.errorsForCurrentRequest.add(
            WebViewError(
                code = withError.code.toInt(),
                description = withError.localizedDescription,
                // on iOS all errors are from the main frame
                isFromMainFrame = true,
            ),
        )
        KLogger.e {
            "didFailNavigation"
        }
    }

    override fun webView(
        webView: WKWebView,
        decidePolicyForNavigationAction: WKNavigationAction,
        decisionHandler: (WKNavigationActionPolicy) -> Unit,
    ) {
        val url = decidePolicyForNavigationAction.request.URL?.absoluteString
        KLogger.info {
            "Outer decidePolicyForNavigationAction: $url $isRedirect $decidePolicyForNavigationAction"
        }
        if (url != null && !isRedirect &&
            navigator.requestInterceptor != null &&
            decidePolicyForNavigationAction.targetFrame?.mainFrame != false
        ) {
            navigator.requestInterceptor.apply {
                val request = decidePolicyForNavigationAction.request
                val headerMap = mutableMapOf<String, String>()
                request.allHTTPHeaderFields?.forEach {
                    headerMap[it.key.toString()] = it.value.toString()
                }
                KLogger.info {
                    "decidePolicyForNavigationAction: ${request.URL?.absoluteString}, $headerMap"
                }
                val webRequest =
                    WebRequest(
                        request.URL?.absoluteString ?: "",
                        headerMap,
                        decidePolicyForNavigationAction.targetFrame?.mainFrame ?: false,
                        isRedirect,
                        request.HTTPMethod ?: "GET",
                    )
                val interceptResult =
                    navigator.requestInterceptor.onInterceptUrlRequest(
                        webRequest,
                        navigator,
                    )
                when (interceptResult) {
                    is WebRequestInterceptResult.Allow -> {
                        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
                    }

                    is WebRequestInterceptResult.Reject -> {
                        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                    }

                    is WebRequestInterceptResult.Modify -> {
                        isRedirect = true
                        interceptResult.request.apply {
                            navigator.stopLoading()
                            navigator.loadUrl(this.url, this.headers)
                        }
                        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                    }
                }
            }
        } else {
            isRedirect = false
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
        }
    }
}
