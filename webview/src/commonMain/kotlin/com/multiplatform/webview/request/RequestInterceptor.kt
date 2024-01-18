package com.multiplatform.webview.request

import com.multiplatform.webview.web.WebViewNavigator

/**
 * Created By Kevin Zou On 2023/11/29
 */
interface RequestInterceptor {
    fun onInterceptRequest(
        request: WebRequest,
        navigator: WebViewNavigator,
    ): WebRequestInterceptResult
}
