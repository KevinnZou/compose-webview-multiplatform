package com.multiplatform.webview.web

import com.multiplatform.webview.jsbridge.WebViewJsBridge
import com.multiplatform.webview.util.KLogger
import compose_webview_multiplatform.webview.generated.resources.Res
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * Created By Kevin Zou On 2023/9/5
 */

/**
 * Interface for WebView
 */
interface IWebView {
    val scope: CoroutineScope

    val webViewJsBridge: WebViewJsBridge?

    /**
     * True when the web view is able to navigate backwards, false otherwise.
     */
    fun canGoBack(): Boolean

    /**
     * True when the web view is able to navigate forwards, false otherwise.
     */
    fun canGoForward(): Boolean

    /**
     * Loads the given URL.
     *
     * @param url The URL of the resource to load.
     */
    fun loadUrl(
        url: String,
        additionalHttpHeaders: Map<String, String> = emptyMap(),
    )

    /**
     * Loads the given HTML string.
     *
     * @param html The HTML string to load.
     * @param baseUrl The URL to use as the page's base URL.
     * @param mimeType The MIME type of the data in the string.
     * @param encoding The encoding of the data in the string.
     * @param historyUrl The history URL for the loaded HTML. Leave null to use about:blank.
     */
    fun loadHtml(
        html: String? = null,
        baseUrl: String? = null,
        mimeType: String? = "text/html",
        encoding: String? = "utf-8",
        historyUrl: String? = null,
    )

    suspend fun loadContent(content: WebContent) {
        when (content) {
            is WebContent.Url ->
                loadUrl(
                    content.url,
                    content.additionalHttpHeaders,
                )

            is WebContent.Data ->
                loadHtml(
                    content.data,
                    content.baseUrl,
                    content.mimeType,
                    content.encoding,
                    content.historyUrl,
                )

            is WebContent.File ->
                loadHtmlFile(
                    content.fileName,
                )

            is WebContent.Post ->
                postUrl(
                    content.url,
                    content.postData,
                )

            is WebContent.NavigatorOnly -> {}
        }
    }

    /**
     * Loads the given HTML file.
     * The file should be placed in the resources folder.
     * It should not contains external links to css or js files.
     * Otherwise, use [loadHtmlFile] instead.
     *
     * @param fileName The name of the HTML file to load.
     */
    @OptIn(ExperimentalResourceApi::class)
    suspend fun loadRawHtmlFile(fileName: String) {
        val html = Res.readBytes(fileName).decodeToString().trimIndent()
        loadHtml(html, encoding = "utf-8")
    }

    /**
     * Loads the given HTML file.
     * The file should be placed in the commonMain/resources/assets folder.
     * It supports external links to css or js files on Android and iOS.
     * But it is not supported on desktop platform because it is not supported by CEF currently.
     *
     * @param fileName The name of the HTML file to load.
     */
    suspend fun loadHtmlFile(fileName: String)

    /**
     * Posts the given data to the given URL.
     *
     * @param url The URL to post the data to.
     * @param postData The data to post.
     */
    fun postUrl(
        url: String,
        postData: ByteArray,
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
    fun evaluateJavaScript(
        script: String,
        callback: ((String) -> Unit)? = null,
    )

    /**
     * Injects the initialization JavaScript code for JSBridge setup
     * into the currently displayed page.
     */
    fun injectJsBridge() {
        if (webViewJsBridge == null) return
        val jsBridgeName = webViewJsBridge!!.jsBridgeName
        KLogger.d {
            "IWebView injectJsBridge"
        }
        val initJs =
            """
            window.$jsBridgeName = {
                callbacks: {},
                callbackId: 0,
                callNative: function (methodName, params, callback) {
                    var message = {
                        methodName: methodName,
                        params: params,
                        callbackId: callback ? window.$jsBridgeName.callbackId++ : -1
                    };
                    if (callback) {
                        window.$jsBridgeName.callbacks[message.callbackId] = callback;
                        console.log('add callback: ' + message.callbackId + ', ' + callback);
                    }
                    window.$jsBridgeName.postMessage(JSON.stringify(message));
                },
                onCallback: function (callbackId, data) {
                    var callback = window.$jsBridgeName.callbacks[callbackId];
                    console.log('onCallback: ' + callbackId + ', ' + data + ', ' + callback);
                    if (callback) {
                        callback(data);
                        delete window.$jsBridgeName.callbacks[callbackId];
                    }
                }
            };
            """.trimIndent()
        evaluateJavaScript(initJs)
    }

    /**
     * Inject the JSBridge into the WebView.
     */
    fun initJsBridge(webViewJsBridge: WebViewJsBridge)

    /**
     * Initialize the WebView.
     */
    fun initWebView() {
        webViewJsBridge?.apply {
            initJsBridge(this)
        }
    }
}
