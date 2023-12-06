package com.multiplatform.webview.jsbridge

import com.multiplatform.webview.web.IWebView
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource

/**
 * Created By Kevin Zou On 2023/10/31
 */
class JsBridge {
    private val jsDispatcher = JsDispatcher()
    private var initJs = ""
    var webView: IWebView? = null

    fun register(handler: IJsHandler) {
        jsDispatcher.registerJSHandler(handler)
    }

    fun unregister(handler: IJsHandler) {
        jsDispatcher.unregisterJSHandler(handler)
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun injectInitJS() {
        if (initJs.isEmpty())
            {
                val res = resource("jsbridge.js")
                initJs = res.readBytes().decodeToString()
            }
        webView?.evaluateJavaScript(initJs)
    }

    fun dispatch(message: JsMessage) {
        jsDispatcher.dispatch(message) {
            onCallback(it, message.id)
        }
    }

    private fun onCallback(
        data: Any,
        callbackId: Int,
    ) {
//        val res = Json.encodeToString(data)
        val res = data.toString()
        webView?.evaluateJavaScript("window.JsBridge.onCallback($callbackId, '$res')")
    }
}
