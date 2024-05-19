package com.multiplatform.webview.request

import com.multiplatform.webview.web.WebViewNavigator

/**
 * Interface for intercepting requests in WebView.
 */
interface RequestInterceptor {
    fun onInterceptUrlRequest(
        request: WebRequest,
        navigator: WebViewNavigator,
    ): WebRequestInterceptResult
}
