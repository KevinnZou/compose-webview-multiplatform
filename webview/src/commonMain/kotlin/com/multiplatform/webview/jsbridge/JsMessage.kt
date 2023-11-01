package com.multiplatform.webview.jsbridge

/**
 * Created By Kevin Zou On 2023/10/31
 */
data class JsMessage(
    val id: Int,
    val methodName: String,
    val params: String,
)
