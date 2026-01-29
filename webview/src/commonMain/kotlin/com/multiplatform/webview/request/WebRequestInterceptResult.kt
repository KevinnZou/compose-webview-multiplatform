package com.multiplatform.webview.request

/**
 * Created By Kevin Zou On 2023/11/30
 */
sealed interface WebRequestInterceptResult {
    data object Allow : WebRequestInterceptResult

    data object Reject : WebRequestInterceptResult

    class Modify(
        val request: WebRequest,
    ) : WebRequestInterceptResult

    /**
     * Respond with custom data instead of making a network request.
     * This allows implementing custom URL schemes or serving local content.
     *
     * Platform support:
     * - **iOS**: Supported via custom URL scheme handler registered with [WKURLSchemeHandler].
     *   Use [PlatformWebViewParams.customSchemes] to register your custom schemes.
     * - **Android**: Not currently implemented. The Respond result will be logged as a warning
     *   and the request will be rejected. Future implementation would use shouldInterceptRequest.
     * - **Desktop**: Not supported. The Respond result will be logged as a warning
     *   and the request will be rejected.
     *
     * @param data The response body as a byte array
     * @param mimeType The MIME type of the response (e.g., "text/html", "application/json").
     *                 This takes precedence over any "Content-Type" header in [headers].
     * @param statusCode The HTTP status code (default: 200)
     * @param headers Optional response headers. Note: "Content-Type" will be overridden by [mimeType].
     */
    class Respond(
        val data: ByteArray,
        val mimeType: String,
        val statusCode: Int = 200,
        val headers: Map<String, String> = emptyMap(),
    ) : WebRequestInterceptResult
}
