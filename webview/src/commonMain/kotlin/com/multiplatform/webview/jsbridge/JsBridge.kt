package com.multiplatform.webview.jsbridge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.multiplatform.webview.web.IWebView

/**
 * Created By Kevin Zou On 2023/10/31
 */
open class JsBridge {
    private val jsDispatcher = JsDispatcher()
    private var initJs = ""
    var webView: IWebView? = null

    fun register(handler: IJsHandler) {
        jsDispatcher.registerJSHandler(handler)
    }

    fun unregister(handler: IJsHandler) {
        jsDispatcher.unregisterJSHandler(handler)
    }

    fun dispatch(message: JsMessage) {
        jsDispatcher.dispatch(message) {
            onCallback(it, message.callbackId)
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

@Composable
fun rememberWebViewJsBridge(): JsBridge =
    remember { JsBridge() }