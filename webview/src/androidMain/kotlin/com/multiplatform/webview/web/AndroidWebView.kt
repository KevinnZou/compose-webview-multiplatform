package com.multiplatform.webview.web

import android.webkit.WebView
import com.multiplatform.webview.util.KLogger

/**
 * Created By Kevin Zou On 2023/9/5
 */
/**
 * Android implementation of [IWebView]
 */
class AndroidWebView(private val webView: WebView) : IWebView {
    override fun canGoBack() = webView.canGoBack()

    override fun canGoForward() = webView.canGoForward()

    override fun loadUrl(url: String, additionalHttpHeaders: Map<String, String>) {
        webView.loadUrl(url, additionalHttpHeaders)
    }

    override fun loadHtml(
        html: String?,
        baseUrl: String?,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?
    ) {
        if (html == null) return
        webView.loadDataWithBaseURL(baseUrl, html, mimeType, encoding, historyUrl)
    }

    override fun postUrl(url: String, postData: ByteArray) {
        webView.postUrl(url, postData)
    }

    override fun goBack() {
        webView.goBack()
    }

    override fun goForward() {
        webView.goForward()
    }

    override fun reload() {
        webView.reload()
    }

    override fun stopLoading() {
        webView.stopLoading()
    }

    override fun evaluateJavaScript(script: String, callback: ((String) -> Unit)?) {
        val androidScript = "javascript:$script"
        KLogger.i {
            "evaluateJavaScript: $androidScript"
        }
        webView.evaluateJavascript(androidScript, callback)
    }

}