package com.multiplatform.webview.jsbridge

/**
 * Created By Kevin Zou On 2023/10/31
 */
class JsDispatcher {
    private val jsHandlerMap = mutableMapOf<String, IJsHandler>()

    fun registerJSHandler(handler: IJsHandler) {
        jsHandlerMap[handler.id()] = handler
    }

    fun dispatch(
        message: JsMessage,
        callback: (Any) -> Unit,
    ) {
        jsHandlerMap[message.methodName]?.handle(message, callback)
    }

    fun canHandle(id: String) = jsHandlerMap.containsKey(id)

    fun unregisterJSHandler(handler: IJsHandler) {
        jsHandlerMap.remove(handler.id())
    }
}
