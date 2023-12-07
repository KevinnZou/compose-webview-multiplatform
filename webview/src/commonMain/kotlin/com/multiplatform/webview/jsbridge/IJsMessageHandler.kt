package com.multiplatform.webview.jsbridge

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Created By Kevin Zou On 2023/10/31
 */
interface IJsMessageHandler {
    fun methodName(): String

    fun canHandle(methodName: String) = methodName() == methodName

    fun handle(
        message: JsMessage,
        callback: (String) -> Unit,
    )

}

inline fun <reified T : Any> IJsMessageHandler.processParams(message: JsMessage): T {
    return Json.decodeFromString(message.params)
}

inline fun <reified T : Any> IJsMessageHandler.dataToJsonString(res: T): String {
    return Json.encodeToString(res)
}
