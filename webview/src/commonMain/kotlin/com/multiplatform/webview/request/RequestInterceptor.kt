package com.multiplatform.webview.request

/**
 * Created By Kevin Zou On 2023/11/29
 */
interface RequestInterceptor {
    fun beforeRequest(request: WebRequest): Boolean
}
