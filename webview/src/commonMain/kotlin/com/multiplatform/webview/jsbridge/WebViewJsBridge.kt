package com.multiplatform.webview.jsbridge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import com.multiplatform.webview.web.IWebView
import com.multiplatform.webview.web.WebViewNavigator

/**
 * Created By Kevin Zou On 2023/10/31
 */

/**
 * A bridge that can be used to communicate between native and web.
 */
@Immutable
open class WebViewJsBridge(val navigator: WebViewNavigator? = null, val jsBridgeName: String = "kmpJsBridge") {
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
        webView?.evaluateJavaScript("window.$jsBridgeName.onCallback($callbackId, '$data')")
    }
}

/**
 * Create a [WebViewJsBridge] that is remembered across Compositions.
 */
@Composable
fun rememberWebViewJsBridge(navigator: WebViewNavigator? = null): WebViewJsBridge = remember { WebViewJsBridge(navigator) }
