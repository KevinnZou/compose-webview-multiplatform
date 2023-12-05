package com.multiplatform.webview.jsbridge

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
