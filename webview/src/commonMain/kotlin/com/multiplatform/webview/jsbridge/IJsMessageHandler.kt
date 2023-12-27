package com.multiplatform.webview.jsbridge

import com.multiplatform.webview.web.WebViewNavigator
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Created By Kevin Zou On 2023/10/31
 */

/**
 * The Interface for handling JS messages.
 */
interface IJsMessageHandler {
    /**
     * The name of the method that will be called on the JS side.
     */
    fun methodName(): String

    fun canHandle(methodName: String) = methodName() == methodName

    /**
     * The logic to handle the JS message.
     * @param message The message that was dispatched from JS.
     * @param navigator The navigator that can be used to control the WebView.
     * @param callback The callback that can be used to send data back to JS.
     */
    fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit,
    )
}

/**
 * Decode the params of [JsMessage] to the given type.
 */
inline fun <reified T : Any> IJsMessageHandler.processParams(message: JsMessage): T {
    return Json.decodeFromString(message.params)
}

/**
 * Encode the given data to a JSON string.
 */
inline fun <reified T : Any> IJsMessageHandler.dataToJsonString(res: T): String {
    return Json.encodeToString(res)
}
