package com.multiplatform.webview.download

import com.multiplatform.webview.web.WebViewNavigator

interface DownloadInterceptor {
    fun onInterceptDownloadRequest(
        request: DownloadRequest,
        navigator: WebViewNavigator,
    )
}
