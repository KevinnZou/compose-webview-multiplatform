package com.multiplatform.webview.request

/**
 * Created By Kevin Zou On 2023/11/29
 */
data class WebRequest(var url: String? = null, var headers: MutableMap<String, String>? = null)
