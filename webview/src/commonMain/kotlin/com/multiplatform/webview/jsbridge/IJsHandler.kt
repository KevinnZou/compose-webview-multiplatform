package com.multiplatform.webview.jsbridge

/**
 * Created By Kevin Zou On 2023/10/31
 */
interface IJsHandler {
    fun id(): String

    fun canHandle(id: String) = id() == id

    fun handle(
        message: JsMessage,
        callback: (Any) -> Unit,
    )
}
