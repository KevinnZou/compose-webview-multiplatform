package com.multiplatform.webview.jsbridge

import kotlinx.serialization.Serializable

/**
 * Created By Kevin Zou On 2023/10/31
 */

/**
 * A message dispatched from JS to native.
 * @param callbackId The callback id that will be used to send data back to JS.
 * @param methodName The name of the method that will be called on the JS side.
 * @param params The parameters that will be passed to the JS method. This should be a JSON string.
 */
@Serializable
data class JsMessage(
    val callbackId: Int,
    val methodName: String,
    val params: String,
)
