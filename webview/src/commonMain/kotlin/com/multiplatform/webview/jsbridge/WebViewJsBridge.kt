package com.multiplatform.webview.jsbridge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.multiplatform.webview.web.IWebView
import com.multiplatform.webview.web.WebViewNavigator

/**
 * Created By Kevin Zou On 2023/10/31
 */
open class WebViewJsBridge(val navigator: WebViewNavigator? = null) {
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
        jsMessageDispatcher.dispatch(message, navigator) {
            onCallback(it, message.callbackId)
        }
    }

    private fun onCallback(
        data: String,
        callbackId: Int,
    ) {
        webView?.evaluateJavaScript("window.kmpJsBridge.onCallback($callbackId, '$data')")
    }
}

@Composable
fun rememberWebViewJsBridge(navigator: WebViewNavigator? = null): WebViewJsBridge = remember { WebViewJsBridge(navigator) }
