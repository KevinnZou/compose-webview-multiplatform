package com.multiplatform.webview.request

sealed interface RequestResult {
    data object Allow : RequestResult
    data object Reject : RequestResult
    data class Modify(val url: String, val additionalHeaders: Map<String, String> = emptyMap()) : RequestResult
}
