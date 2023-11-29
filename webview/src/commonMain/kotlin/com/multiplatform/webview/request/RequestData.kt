package com.multiplatform.webview.request


data class RequestData (
    val url: String,
    val isForMainFrame: Boolean,
    val isRedirect: Boolean,

    val method: String,
    val requestHeaders: Map<String, String>
)
