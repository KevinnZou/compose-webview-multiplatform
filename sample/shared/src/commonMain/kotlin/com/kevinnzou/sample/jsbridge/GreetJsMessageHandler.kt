package com.kevinnzou.sample.jsbridge

import co.touchlab.kermit.Logger
import com.kevinnzou.sample.model.GreetModel
import com.multiplatform.webview.jsbridge.IJsMessageHandler
import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.jsbridge.processParams

/**
 * Created By Kevin Zou On 2023/12/6
 */
class GreetJsMessageHandler : IJsMessageHandler {
    override fun methodName(): String {
        return "Greet"
    }

    override fun handle(message: JsMessage, callback: (Any) -> Unit) {
        Logger.i {
            "Greet Handler Get Message: $message"
        }
        val param = processParams<GreetModel>(message)
        callback("KMM ${param.type}")
    }
}