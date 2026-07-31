package com.multiplatform.webview.download;

import android.util.Base64
import android.webkit.JavascriptInterface
import com.multiplatform.webview.web.WebViewNavigator
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

internal class BlobDownloadInterface(private val navigator: WebViewNavigator) {

    @JavascriptInterface
    fun downloadBlob(base64Data: String, jsonString: String = "") {
        try {
            val pureBase64 = if (base64Data.contains(",")) base64Data.split(",")[1] else base64Data
            val fileBytes = Base64.decode(pureBase64, Base64.DEFAULT)

            val webRequest =
                DownloadRequest(
                    url = "",
                    headers = mutableMapOf()
                )

            navigator.downloadInterceptor?.onInterceptDownloadResponse(
                request = webRequest,
                navigator = navigator,
                byteArray = fileBytes,
                json = Json.parseToJsonElement(jsonString).jsonObject
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}