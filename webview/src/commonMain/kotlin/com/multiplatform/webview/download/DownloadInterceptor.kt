package com.multiplatform.webview.download

import com.multiplatform.webview.web.WebViewNavigator
import kotlinx.serialization.json.JsonObject

interface DownloadInterceptor {
    fun onInterceptDownloadResponse(
        request: DownloadRequest,
        navigator: WebViewNavigator,
        byteArray: ByteArray,
        json: JsonObject?
    )

    fun onRequiredOverrideJavascriptInterface(
        request: DownloadRequest,
        navigator: WebViewNavigator,
        scriptCommand: String
    ): String
}
