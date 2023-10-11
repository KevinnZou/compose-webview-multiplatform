package com.multiplatform.webview.web

import co.touchlab.kermit.Logger
import platform.Foundation.NSError
import platform.WebKit.WKNavigation
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
    private val navigator: WebViewNavigator
) : NSObject(), WKNavigationDelegateProtocol {
    /**
     * Called when the web view begins to receive web content.
     */
    override fun webView(
        webView: WKWebView,
        didStartProvisionalNavigation: WKNavigation?
    ) {
        state.loadingState = LoadingState.Loading(0f)
        state.lastLoadedUrl = webView.URL.toString()
        state.errorsForCurrentRequest.clear()
        Logger.i {
            "didStartProvisionalNavigation"
        }
    }

    /**
     * Called when the web view receives a server redirect.
     */
    override fun webView(
        webView: WKWebView,
        didCommitNavigation: WKNavigation?
    ) {
        state.loadingState =
            LoadingState.Loading(webView.estimatedProgress.toFloat())
        Logger.i { "didCommitNavigation" }
    }

    /**
     * Called when the web view finishes loading.
     */
    override fun webView(
        webView: WKWebView,
        didFinishNavigation: WKNavigation?
    ) {
        state.pageTitle = webView.title
        state.lastLoadedUrl = webView.URL.toString()
        state.loadingState = LoadingState.Finished
        navigator.canGoBack = webView.canGoBack
        navigator.canGoForward = webView.canGoForward
        Logger.i { "didFinishNavigation" }
    }

    /**
     * Called when the web view fails to load content.
     */
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
        Logger.i {
            "didFailNavigation"
        }
    }
}