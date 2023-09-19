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
@Suppress("CONFLICTING_OVERLOADS")
class WKNavigationDelegate(
    private val state: WebViewState,
    private val navigator: WebViewNavigator
) : NSObject(), WKNavigationDelegateProtocol {
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

    override fun webView(
        webView: WKWebView,
        didCommitNavigation: WKNavigation?
    ) {
        state.loadingState =
            LoadingState.Loading(webView.estimatedProgress.toFloat())
        Logger.i { "didCommitNavigation" }
    }

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