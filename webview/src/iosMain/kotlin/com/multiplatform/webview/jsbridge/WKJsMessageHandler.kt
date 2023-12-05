package com.multiplatform.webview.jsbridge

import com.multiplatform.webview.util.KLogger
import kotlinx.serialization.json.Json
import platform.WebKit.WKScriptMessage
import platform.WebKit.WKScriptMessageHandlerProtocol
import platform.WebKit.WKUserContentController
import platform.darwin.NSObject

/**
 * Created By Kevin Zou On 2023/11/1
 */
class WKJsMessageHandler(private val jsBridge: JsBridge) : WKScriptMessageHandlerProtocol,
    NSObject() {
    override fun userContentController(
        userContentController: WKUserContentController,
        didReceiveScriptMessage: WKScriptMessage
    ) {
        val body = didReceiveScriptMessage.body
        val method = didReceiveScriptMessage.name
        KLogger.i { "didReceiveScriptMessage: $body, $method" }
        (body as String).apply {
            val message = Json.decodeFromString<JsMessage>(body)
            KLogger.i {
                "WKJsMessageHandler: $message"
            }
            jsBridge.dispatch(message)
        }
    }
}