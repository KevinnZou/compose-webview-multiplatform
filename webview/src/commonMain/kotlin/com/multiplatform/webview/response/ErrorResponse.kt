package com.multiplatform.webview.response

import com.multiplatform.webview.web.WebContent

data class ErrorResponse(
    val description: String? = null,
    val errorCode: Long? = null,
    val url: String? = null
)