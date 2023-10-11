package com.multiplatform.webview.web

/**
 * Created By Kevin Zou On 2023/9/5
 */

interface IWebView {
    /**
     * True when the web view is able to navigate backwards, false otherwise.
     */
    fun canGoBack(): Boolean

    /**
     * True when the web view is able to navigate forwards, false otherwise.
     */
    fun canGoForward(): Boolean

    fun loadUrl(url: String, additionalHttpHeaders: Map<String, String> = emptyMap())

    fun loadHtml(
        html: String? = null,
        baseUrl: String? = null,
        mimeType: String? = "text/html",
        encoding: String? = "utf-8",
        historyUrl: String? = null
    )

    fun loadContent(content: WebContent) {
        when (content) {
            is WebContent.Url -> loadUrl(
                content.url,
                content.additionalHttpHeaders
            )
            is WebContent.Data -> loadHtml(
                content.data,
                content.baseUrl,
                content.mimeType,
                content.encoding,
                content.historyUrl
            )
            is WebContent.Post -> postUrl(
                content.url,
                content.postData
            )

            WebContent.NavigatorOnly -> { }
        }
    }

    fun postUrl(
        url: String,
        postData: ByteArray
    )

    /**
     * Navigates the webview back to the previous page.
     */
    fun goBack()

    /**
     * Navigates the webview forward after going back from a page.
     */
    fun goForward()

    /**
     * Reloads the current page in the webview.
     */
    fun reload()

    /**
     * Stops the current page load (if one is loading).
     */
    fun stopLoading()

    /**
     * Evaluates the given JavaScript in the context of the currently displayed page.
     * and returns the result of the evaluation.
     * Note: The callback will not be called from desktop platform because it is not supported by CEF currently.
     */
    fun evaluateJavaScript(script: String, callback: ((String) -> Unit)? = null)
}