@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.multiplatform.webview.web

import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import com.multiplatform.webview.util.KLogger
import dev.datlag.kcef.KCEFBrowser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefMessageRouter
import org.cef.callback.CefQueryCallback
import org.cef.handler.CefMessageRouterHandlerAdapter
import org.cef.network.CefPostData
import org.cef.network.CefPostDataElement
import org.cef.network.CefRequest
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes

actual typealias NativeWebView = KCEFBrowser

/**
 * Created By Kevin Zou On 2023/9/12
 */
class DesktopWebView(
    override val webView: KCEFBrowser,
    override val scope: CoroutineScope,
    override val webViewJsBridge: WebViewJsBridge?,
) : IWebView {
    init {
        initWebView()
    }

    override fun canGoBack() = webView.canGoBack()

    override fun canGoForward() = webView.canGoForward()

    override fun loadUrl(
        url: String,
        additionalHttpHeaders: Map<String, String>,
    ) {
        if (additionalHttpHeaders.isNotEmpty()) {
            val request =
                CefRequest.create().apply {
                    this.url = url
                    this.setHeaderMap(additionalHttpHeaders)
                }
            webView.loadRequest(request)
        } else {
            KLogger.d {
                "DesktopWebView loadUrl $url"
            }
            webView.loadURL(url)
        }
    }

    override fun loadHtml(
        html: String?,
        baseUrl: String?,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?,
    ) {
        KLogger.d {
            "DesktopWebView loadHtml"
        }
        if (html != null) {
            try {
                webView.loadHtml(html, baseUrl ?: KCEFBrowser.BLANK_URI)
            } catch (e: Exception) {
                KLogger.e { "DesktopWebView loadHtml error: ${e.message}" }
            }
        } else {
            KLogger.e { "DesktopWebView loadHtml: HTML content is null" }
        }
    }

    @OptIn(InternalResourceApi::class)
    override suspend fun loadHtmlFile(fileName: String) {
        try {
            val res = readResourceBytes("assets/$fileName")
            val content = res.decodeToString().trimIndent()

            // Inline external resources (CSS and JS) since we can't use custom base URLs
            val htmlWithInlinedResources = inlineExternalResources(content, fileName)

            // Load HTML content with JS bridge and inline external resources with a bit delay
            delay(200)
            webView.loadHtml(htmlWithInlinedResources)
        } catch (e: Exception) {
            // Load error page using data URL
            val errorHtml = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Error Loading File</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; color: #333; }
                        .error { color: red; }
                    </style>
                </head>
                <body>
                    <h2 class="error">Error Loading File</h2>
                    <p>Could not load file: $fileName</p>
                    <p>Error: ${e.message}</p>
                </body>
                </html>
                """.trimIndent()
            delay(200)
            webView.loadHtml(errorHtml)
        }
    }

    override fun postUrl(
        url: String,
        postData: ByteArray,
    ) {
        val request =
            CefRequest.create().apply {
                this.url = url
                this.postData =
                    CefPostData.create().apply {
                        this.addElement(
                            CefPostDataElement.create().apply {
                                this.setToBytes(postData.size, postData)
                            },
                        )
                    }
            }
        webView.loadRequest(request)
    }

    override fun goBack() = webView.goBack()

    override fun goForward() = webView.goForward()

    override fun reload() = webView.reload()

    override fun stopLoading() = webView.stopLoad()

    override fun evaluateJavaScript(
        script: String,
        callback: ((String) -> Unit)?,
    ) {
        KLogger.d {
            "evaluateJavaScript: $script"
        }
        webView.evaluateJavaScript(script) {
            if (it != null) {
                callback?.invoke(it)
            }
        }
    }

    override fun injectJsBridge() {
        if (webViewJsBridge == null) return
        super.injectJsBridge()
        KLogger.d {
            "DesktopWebView injectJsBridge"
        }
        val callDesktop =
            """
            window.${webViewJsBridge.jsBridgeName}.postMessage = function (message) {
                    window.cefQuery({request:message});
                };
            """.trimIndent()
        evaluateJavaScript(callDesktop)
    }

    override fun initJsBridge(webViewJsBridge: WebViewJsBridge) {
        KLogger.d {
            "DesktopWebView initJsBridge"
        }
        val router = CefMessageRouter.create()
        val handler =
            object : CefMessageRouterHandlerAdapter() {
                override fun onQuery(
                    browser: CefBrowser?,
                    frame: CefFrame?,
                    queryId: Long,
                    request: String?,
                    persistent: Boolean,
                    callback: CefQueryCallback?,
                ): Boolean {
                    if (request == null) {
                        return super.onQuery(
                            browser,
                            frame,
                            queryId,
                            request,
                            persistent,
                            callback,
                        )
                    }
                    val message = Json.decodeFromString<JsMessage>(request)
                    KLogger.d {
                        "onQuery Message: $message"
                    }
                    webViewJsBridge.dispatch(message)
                    return true
                }
            }
        router.addHandler(handler, false)
        webView.client.addMessageRouter(router)
    }

    @OptIn(InternalResourceApi::class)
    private fun inlineExternalResources(htmlContent: String, baseFileName: String): String {
        var modifiedHtml = htmlContent

        // Extract base path for relative resources
        val basePath = if (baseFileName.contains("/")) {
            baseFileName.substringBeforeLast("/") + "/"
        } else {
            ""
        }

        try {
            // Inline CSS files
            val cssPattern = """<link\s+[^>]*href\s*=\s*["']([^"']+\.css)["'][^>]*>""".toRegex(RegexOption.IGNORE_CASE)
            modifiedHtml = cssPattern.replace(modifiedHtml) { matchResult ->
                val cssFile = matchResult.groupValues[1]
                try {
                    val cssRes = runBlocking { readResourceBytes("assets/$basePath$cssFile") }
                    val cssContent = cssRes.decodeToString()
                    "<style>\n$cssContent\n</style>"
                } catch (e: Exception) {
                    KLogger.e { "DesktopWebView: Could not inline CSS $cssFile: ${e.message}" }
                    matchResult.value // Keep original if can't inline
                }
            }

            // Inline JS files
            val jsPattern = """<script\s+[^>]*src\s*=\s*["']([^"']+\.js)["'][^>]*></script>""".toRegex(RegexOption.IGNORE_CASE)
            modifiedHtml = jsPattern.replace(modifiedHtml) { matchResult ->
                val jsFile = matchResult.groupValues[1]
                try {
                    val jsRes = runBlocking { readResourceBytes("assets/$basePath$jsFile") }
                    val jsContent = jsRes.decodeToString()
                    "<script>\n$jsContent\n</script>"
                } catch (e: Exception) {
                    KLogger.e { "DesktopWebView: Could not inline JS $jsFile: ${e.message}" }
                    matchResult.value // Keep original if can't inline
                }
            }

        } catch (e: Exception) {
            KLogger.e { "DesktopWebView: Error during resource inlining: ${e.message}" }
        }

        return modifiedHtml
    }

    override fun saveState(): WebViewBundle? {
        return null
    }

    override fun scrollOffset(): Pair<Int, Int> {
        return Pair(0, 0)
    }
}
