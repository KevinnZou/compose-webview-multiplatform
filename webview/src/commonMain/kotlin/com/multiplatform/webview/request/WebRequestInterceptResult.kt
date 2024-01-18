package com.multiplatform.webview.request

/**
 * Created By Kevin Zou On 2023/11/30
 */
sealed interface WebRequestInterceptResult {
    data object Allow : WebRequestInterceptResult

    data object Reject : WebRequestInterceptResult

    class Modify(val request: WebRequest) : WebRequestInterceptResult
}
