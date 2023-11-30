package com.multiplatform.webview.web

import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.request.WebRequestInterceptResult
import com.multiplatform.webview.util.KLogger
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
    private var lastUrl = ""
    private var lastInterceptUrl = ""

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
        KLogger.d {
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
        KLogger.d { "didCommitNavigation" }
    }

    /**
     * Called when the web view finishes loading.
     */
    override fun webView(
        webView: WKWebView,
        didFinishNavigation: WKNavigation?,
    ) {
        state.pageTitle = webView.title
        state.lastLoadedUrl = webView.URL.toString()
        state.loadingState = LoadingState.Finished
        navigator.canGoBack = webView.canGoBack
        navigator.canGoForward = webView.canGoForward
        KLogger.d { "didFinishNavigation" }
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
                withError.code.toInt(),
                withError.localizedDescription,
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
        if (url != null && lastUrl != url && lastInterceptUrl != url &&
            navigator.requestInterceptor != null
        ) {
            lastInterceptUrl = url
            navigator.requestInterceptor.apply {
                val request = decidePolicyForNavigationAction.request
                val headerMap = mutableMapOf<String, String>()
                request.allHTTPHeaderFields?.forEach {
                    headerMap[it.key.toString()] = it.value.toString()
                }
                KLogger.i {
                    "decidePolicyForNavigationAction: ${request.URL?.absoluteString}, $headerMap"
                }
                val webRequest =
                    WebRequest(
                        request.URL?.absoluteString ?: "",
                        headerMap,
                    )
                val interceptResult =
                    navigator.requestInterceptor.beforeRequest(
                        webRequest,
                        navigator,
                    )
                return when (interceptResult) {
                    is WebRequestInterceptResult.Allow -> {
                        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
                    }

                    is WebRequestInterceptResult.Reject -> {
                        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                    }

                    is WebRequestInterceptResult.Redirect -> {
                        interceptResult.request.apply {
                            navigator.loadUrl(this.url, this.headers)
                            lastUrl = this.url
                        }
                        decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyCancel)
                    }
                }
            }
        } else {
            decisionHandler(WKNavigationActionPolicy.WKNavigationActionPolicyAllow)
        }
    }
}
