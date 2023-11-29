package com.multiplatform.webview.request

import com.multiplatform.webview.web.WebViewNavigator

/**
 * Created By Kevin Zou On 2023/11/29
 */
interface RequestInterceptor {
    fun beforeRequest(
        request: WebRequest,
        navigator: WebViewNavigator,
    ): Boolean
}
