package com.multiplatform.webview.web

import com.multiplatform.webview.jsbridge.JsBridge
import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.util.KLogger
import dev.datlag.kcef.KCEFBrowser
import kotlinx.coroutines.CoroutineScope
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefMessageRouter
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefMessageRouterHandlerAdapter
import org.cef.network.CefPostData
import org.cef.network.CefPostDataElement
import org.cef.network.CefRequest

/**
 * Created By Kevin Zou On 2023/9/12
 */
class DesktopWebView(
    private val webView: KCEFBrowser,
    override var scope: CoroutineScope,
    override var jsBridge: JsBridge,
) : IWebView {
    init {
        initWebView()
    }

    override fun canGoBack() = webView.canGoBack()

    override fun canGoForward() = webView.canGoForward()

    override fun loadUrl(
        url: String,
        additionalHttpHeaders: Map<String, String>,
    ) {
        if (additionalHttpHeaders.isNotEmpty()) {
            val request =
                CefRequest.create().apply {
                    this.url = url
                    this.setHeaderMap(additionalHttpHeaders)
                }
            webView.loadRequest(request)
        } else {
            webView.loadURL(url)
        }
    }

    override fun loadHtml(
        html: String?,
        baseUrl: String?,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?,
    ) {
        KLogger.d {
            "DesktopWebView loadHtml"
        }
        if (html != null) {
            webView.loadHtml(html, baseUrl ?: KCEFBrowser.BLANK_URI)
        }
    }

    override suspend fun loadHtmlFile(fileName: String) {
        // TODO
    }

    override fun postUrl(
        url: String,
        postData: ByteArray,
    ) {
        val request =
            CefRequest.create().apply {
                this.url = url
                this.postData =
                    CefPostData.create().apply {
                        this.addElement(
                            CefPostDataElement.create().apply {
                                this.setToBytes(postData.size, postData)
                            },
                        )
                    }
            }
        webView.loadRequest(request)
    }

    override fun goBack() = webView.goBack()

    override fun goForward() = webView.goForward()

    override fun reload() = webView.reload()

    override fun stopLoading() = webView.stopLoad()

    override fun evaluateJavaScript(
        script: String,
        callback: ((String) -> Unit)?,
    ) {
        KLogger.d {
            "evaluateJavaScript: $script"
        }
        webView.evaluateJavaScript(script) {
            if (it != null) {
                callback?.invoke(it)
            }
        }
    }

    override fun injectBridge(jsBridge: JsBridge) {
        val router = CefMessageRouter.create()
        val handler = object : CefMessageRouterHandlerAdapter() {
            override fun onQuery(
                browser: CefBrowser?,
                frame: CefFrame?,
                queryId: Long,
                request: String?,
                persistent: Boolean,
                callback: CefQueryCallback?
            ): Boolean {
                KLogger.d {
                    "onQuery: $request"
                }
                val id = request?.substringBefore('_')
                val methodName = request?.substringAfter('_')?.substringBefore('_')
                val params = request?.substringAfterLast('_')
                val message = JsMessage(
                    id?.toInt() ?: 0,
                    methodName ?: "",
                    params ?: "",
                )
                KLogger.d {
                    "onQuery Message: $message"
                }
                jsBridge.dispatch(message)
                return super.onQuery(browser, frame, queryId, request, persistent, callback)
            }
        }
        router.addHandler(handler, false)
        webView.client.addMessageRouter(router)
    }
}
