package com.multiplatform.webview.jsbridge

import kotlinx.serialization.json.Json

/**
 * Created By Kevin Zou On 2023/10/31
 */
interface IJsHandler {
    fun methodName(): String

    fun canHandle(methodName: String) = methodName() == methodName

    fun handle(
        message: JsMessage,
        callback: (Any) -> Unit,
    )

}

inline fun <reified T : Any> IJsHandler.processParams(message: JsMessage): T {
    return Json.decodeFromString(message.params)
}
