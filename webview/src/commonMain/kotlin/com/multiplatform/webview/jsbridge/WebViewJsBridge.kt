package com.multiplatform.webview.jsbridge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.multiplatform.webview.web.IWebView

/**
 * Created By Kevin Zou On 2023/10/31
 */
open class WebViewJsBridge {
    private val jsMessageDispatcher = JsMessageDispatcher()
    var webView: IWebView? = null

    fun register(handler: IJsMessageHandler) {
        jsMessageDispatcher.registerJSHandler(handler)
    }

    fun unregister(handler: IJsMessageHandler) {
        jsMessageDispatcher.unregisterJSHandler(handler)
    }

    fun clear() {
        jsMessageDispatcher.clear()
    }

    fun dispatch(message: JsMessage) {
        jsMessageDispatcher.dispatch(message) {
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
fun rememberWebViewJsBridge(): WebViewJsBridge =
    remember { WebViewJsBridge() }