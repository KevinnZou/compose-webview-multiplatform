package com.multiplatform.webview.jsbridge

import com.multiplatform.webview.web.IWebView
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource

/**
 * Created By Kevin Zou On 2023/10/31
 */
class JsBridge {
    private val jsDispatcher = JsDispatcher()
    private var initJs = ""
    private var webView: IWebView? = null

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

    fun onCallback(
        data: Any,
        callbackId: Int,
    ) {
        val res = Json.encodeToString(data)
        webView?.evaluateJavaScript("window.jsBridge.onCallback('$callbackId', $res)")
    }
}
