package com.multiplatform.webview.download

data class DownloadRequest(
    val url: String,
    val headers: MutableMap<String, String> = mutableMapOf()
)