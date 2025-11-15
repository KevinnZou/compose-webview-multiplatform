package com.multiplatform.webview.response

import com.multiplatform.webview.web.WebViewNavigator

typealias ShouldStopLoading = Boolean
interface ErrorResponseInterceptor {
    fun onInterceptErrorResponse(
        response: ErrorResponse,
        navigator: WebViewNavigator,
    ): ShouldStopLoading
}