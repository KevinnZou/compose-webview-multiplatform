package com.multiplatform.webview.request

/**
 * Created By Kevin Zou On 2023/11/29
 */
data class WebRequest(
    val url: String,
    val headers: MutableMap<String, String> = mutableMapOf(),
    val isForMainFrame: Boolean = false,
    val isRedirect: Boolean = false,
    val method: String = "GET",
)
