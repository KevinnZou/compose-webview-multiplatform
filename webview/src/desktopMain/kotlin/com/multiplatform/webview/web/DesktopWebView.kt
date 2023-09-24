package com.multiplatform.webview.web

import co.touchlab.kermit.Logger
import org.cef.browser.CefBrowser
import org.cef.network.CefPostData
import org.cef.network.CefPostDataElement
import org.cef.network.CefRequest

/**
 * Created By Kevin Zou On 2023/9/12
 */
class DesktopWebView(private val webView: CefBrowser) : IWebView {

    override fun canGoBack() = webView.canGoBack()

    override fun canGoForward() = webView.canGoForward()

    override fun loadUrl(url: String, additionalHttpHeaders: Map<String, String>) {
        webView.loadURL(url)
    }

    override fun loadHtml(
        html: String?,
        baseUrl: String?,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?
    ) {
        if (html != null) {
            webView.loadHtml(html)
        }
    }

    override fun postUrl(url: String, postData: ByteArray) {
        val request = CefRequest.create().apply {
            this.url = url
            this.postData = CefPostData.create().apply {
                this.addElement(CefPostDataElement.create().apply {
                    this.setToBytes(postData.size, postData)
                })
            }
        }
        webView.loadRequest(request)
    }

    override fun goBack() = webView.goBack()

    override fun goForward() = webView.goForward()

    override fun reload() = webView.reload()

    override fun stopLoading() = webView.stopLoad()

    override fun evaluateJavaScript(script: String, callback: ((String) -> Unit)?) {
        Logger.i {
            "evaluateJavaScript: $script"
        }
        webView.executeJavaScript(script, "", 0)
    }
}