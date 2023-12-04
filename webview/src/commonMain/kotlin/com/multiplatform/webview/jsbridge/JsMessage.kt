package com.multiplatform.webview.jsbridge

import kotlinx.serialization.Serializable

/**
 * Created By Kevin Zou On 2023/10/31
 */
@Serializable
data class JsMessage(
    val id: Int,
    val methodName: String,
    val params: String,
)
