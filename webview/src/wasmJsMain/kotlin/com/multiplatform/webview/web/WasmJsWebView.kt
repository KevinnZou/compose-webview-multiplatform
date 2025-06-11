package com.multiplatform.webview.web

import com.multiplatform.webview.jsbridge.WebViewJsBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.InternalResourceApi
import org.w3c.dom.Element

/**
 * The native web view implementation for WasmJs platform.
 * Uses HTML iframe element as the underlying implementation.
 */
actual class NativeWebView(
    val element: Element,
)

/**
 * WebView adapter for WasmJs that implements the IWebView interface
 */
class WasmJsWebView(
    private val element: Element,
    override val webView: NativeWebView,
    override val scope: CoroutineScope,
    override val webViewJsBridge: WebViewJsBridge?,
) : IWebView {
    override fun canGoBack(): Boolean =
        try {
            checkCanGoBackJs(element)
        } catch (_: Exception) {
            false
        }

    override fun canGoForward(): Boolean =
        try {
            checkCanGoForwardJs(element)
        } catch (_: Exception) {
            false
        }

    override fun loadUrl(
        url: String,
        additionalHttpHeaders: Map<String, String>,
    ) {
        try {
            setUrlJs(element, url)
            if (webViewJsBridge != null) {
                scope.launch {
                    delay(500)
                    injectJsBridge()
                }
            }
        } catch (_: Exception) {
        }
    }

    override suspend fun loadHtml(
        html: String?,
        baseUrl: String?,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?,
    ) {
        try {
            if (html != null) {
                val htmlWithBridge =
                    if (webViewJsBridge != null) {
                        injectBridgeIntoHtml(html, webViewJsBridge.jsBridgeName)
                    } else {
                        html
                    }
                setHtmlContentJs(element, htmlWithBridge)
            }
        } catch (_: Exception) {
        }
    }

    @OptIn(InternalResourceApi::class)
    override suspend fun loadHtmlFile(
        fileName: String,
        readType: WebViewFileReadType,
    ) {
        try {
            val url =
                when (readType) {
                    WebViewFileReadType.ASSET_RESOURCES -> "assets/$fileName"
                    WebViewFileReadType.COMPOSE_RESOURCE_FILES -> fileName
                }
            setUrlJs(element, url)

            if (webViewJsBridge != null) {
                scope.launch {
                    delay(1000)
                    injectJsBridge()
                }
            }
        } catch (e: Exception) {
            val fallbackHtml =
                """
                <!DOCTYPE html>
                <html>
                <body>
                    <h2 style="color:red;">Failed to load file: $fileName</h2>
                    <p>Error: ${e.message}</p>
                </body>
                </html>
                """.trimIndent()
            loadHtml(fallbackHtml, null, null, null, null)
        }
    }

    override fun postUrl(
        url: String,
        postData: ByteArray,
    ) {
        loadUrl(url, emptyMap())
    }

    override fun goBack() {
        try {
            navigateBackJs(element)
        } catch (_: Exception) {
        }
    }

    override fun goForward() {
        try {
            navigateForwardJs(element)
        } catch (_: Exception) {
        }
    }

    override fun reload() {
        try {
            reloadJs(element)
        } catch (_: Exception) {
        }
    }

    override fun stopLoading() {
        try {
            stopLoadingJs(element)
        } catch (e: Exception) {
        }
    }

    override fun evaluateJavaScript(
        script: String,
        callback: ((String) -> Unit)?,
    ) {
        scope.launch {
            try {
                val result = evaluateScriptJs(element, script)
                callback?.invoke(result)
            } catch (e: Exception) {
                callback?.invoke("Error: ${e.message}")
            }
        }
    }

    override fun injectJsBridge() {
        if (webViewJsBridge == null) return
        super.injectJsBridge()

        val bridgeScript = createJsBridgeScript(webViewJsBridge.jsBridgeName, true)
        evaluateJavaScript(bridgeScript)

        val messageHandler: (org.w3c.dom.events.Event) -> Unit = { event ->
            val messageEvent = event as org.w3c.dom.MessageEvent
            val iframe = element as? org.w3c.dom.HTMLIFrameElement

            if (iframe != null &&
                messageEvent.source == iframe.contentWindow &&
                messageEvent.data != null
            ) {
                try {
                    val dataString = messageEvent.data.toString()

                    if (dataString.contains("kmpJsBridge")) {
                        val actionPattern = """action[=:][\s]*['"](.*?)['"]""".toRegex()
                        val paramsPattern = """params[=:][\s]*['"](.*?)['"]""".toRegex()
                        val callbackPattern = """callbackId[=:][\s]*(\d+)""".toRegex()

                        val action = actionPattern.find(dataString)?.groupValues?.get(1)
                        val params = paramsPattern.find(dataString)?.groupValues?.get(1) ?: "{}"
                        val callbackId =
                            callbackPattern
                                .find(dataString)
                                ?.groupValues
                                ?.get(1)
                                ?.toIntOrNull()
                                ?: 0

                        if (action != null) {
                            val message =
                                com.multiplatform.webview.jsbridge.JsMessage(
                                    callbackId = callbackId,
                                    methodName = action,
                                    params = params,
                                )
                            webViewJsBridge.dispatch(message)
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }

        kotlinx.browser.window.addEventListener("message", messageHandler)
        webViewJsBridge.webView = this
    }

    override fun initJsBridge(webViewJsBridge: WebViewJsBridge) {
        // Bridge initialization is handled externally
    }

    override fun saveState(): WebViewBundle? = null

    override fun scrollOffset(): Pair<Int, Int> = Pair(0, 0)

    /**
     * Inject JS bridge script into HTML content - improved version
     */
    private fun injectBridgeIntoHtml(
        htmlContent: String,
        jsBridgeName: String,
    ): String {
        val bridgeScriptContent = createJsBridgeScript(jsBridgeName)
        val bridgeScript =
            """
            <script>
            // KMP WebView Bridge - Must be loaded first
            $bridgeScriptContent
            </script>
            """.trimIndent()

        if (htmlContent.contains("<head>")) {
            return htmlContent.replace("<head>", "<head>$bridgeScript")
        }

        val headPattern = "<head[^>]*>".toRegex()
        val headMatch = headPattern.find(htmlContent)
        if (headMatch != null) {
            return htmlContent.replace(headMatch.value, "${headMatch.value}$bridgeScript")
        }

        if (htmlContent.contains("<body>") || htmlContent.contains("<body ")) {
            val bodyPattern = "<body[^>]*>".toRegex()
            return bodyPattern.replace(htmlContent, "$0$bridgeScript")
        }

        return "$bridgeScript$htmlContent"
    }
}
