package com.multiplatform.webview.request

fun interface RequestInterceptor {
    operator fun invoke(data: RequestData): RequestResult
}