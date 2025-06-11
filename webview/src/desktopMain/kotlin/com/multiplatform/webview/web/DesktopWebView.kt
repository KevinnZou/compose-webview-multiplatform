package com.multiplatform.webview.web

import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import com.multiplatform.webview.util.KLogger
import com.multiplatform.webview.util.tempDirectory
import dev.datlag.kcef.KCEFBrowser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
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
import java.io.File
import java.io.InputStreamReader
import java.net.JarURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets

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

    override suspend fun loadHtml(
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
                delay(500)
                webView.loadHtml(html, baseUrl ?: KCEFBrowser.BLANK_URI)
            } catch (e: Exception) {
                KLogger.e { "DesktopWebView loadHtml error: ${e.message}" }
                e.printStackTrace()
            }
        } else {
            KLogger.e { "DesktopWebView loadHtml: HTML content is null" }
        }
    }

    @OptIn(InternalResourceApi::class)
    override suspend fun loadHtmlFile(
        fileName: String,
        readType: WebViewFileReadType,
    ) {
        var attemptedResourcePath = fileName // For logging in case of error
        try {
            when (readType) {
                WebViewFileReadType.ASSET_RESOURCES -> {
                    val path = fileName.removePrefix("/")
                    attemptedResourcePath = "assets/$path"
                    val inputStream =
                        this::class.java.classLoader.getResourceAsStream(attemptedResourcePath)
                    if (inputStream == null) {
                        throw Exception("Resource not found: $attemptedResourcePath (for readType: $readType)")
                    }

                    val outFile = File(tempDirectory, path.substringAfterLast("/"))
                    outFile.outputStream().use { output ->
                        inputStream.copyTo(output)
                    }

                    val baseFolder = attemptedResourcePath.substringBeforeLast("/", "")
                    val basePath = if (baseFolder.isNotEmpty()) "$baseFolder/" else ""

                    // Copy other assets from same folder if any
                    val resourceUrls = this::class.java.classLoader.getResources(basePath)
                    while (resourceUrls.hasMoreElements()) {
                        val url = resourceUrls.nextElement()
                        val connection = url.openConnection()
                        if (connection is JarURLConnection) {
                            val jarFile = connection.jarFile
                            for (entry in jarFile.entries()) {
                                if (entry.name.startsWith(basePath) && !entry.isDirectory) {
                                    val file =
                                        File(tempDirectory, entry.name.substringAfterLast("/"))
                                    if (!file.exists()) {
                                        jarFile.getInputStream(entry).use { input ->
                                            file.outputStream().use { input.copyTo(it) }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    delay(500)
                    webView.loadURL("file://${outFile.absolutePath}")
                }

                WebViewFileReadType.COMPOSE_RESOURCE_FILES -> {
                    // fileName is expected to be a URI string from Res.getUri(), like "jar:file:..." or "file:///..."

                    val parts = fileName.split("!/")
                    if (parts.size != 2) {
                        throw Exception("Invalid JAR URI format: $fileName")
                    }
                    val pathInJar = parts[1].removePrefix("/")
                    attemptedResourcePath = pathInJar

                    // Fix: Correct JAR URL parsing for JarURLConnection
                    val jarFileUrl = parts[0].removePrefix("jar:")
                    val jarUrl = URI("jar", "$jarFileUrl!/", null).toURL()
                    val jarConnection = jarUrl.openConnection() as JarURLConnection
                    val jarFile = jarConnection.jarFile

                    for (entry in jarFile.entries()) {
                        if (entry.name.startsWith(pathInJar.substringBeforeLast("/")) && !entry.isDirectory) {
                            val file =
                                File(tempDirectory, entry.name.substringAfterLast("/"))
                            file.outputStream().use { output ->
                                jarFile.getInputStream(entry).copyTo(output)
                            }
                        }
                    }

                    val htmlFile = File(tempDirectory, pathInJar.substringAfterLast("/"))
                    if (!htmlFile.exists()) {
                        throw Exception("Extracted HTML file not found: ${htmlFile.absolutePath}")
                    }

                    delay(500)
                    webView.loadURL("file://${htmlFile.absolutePath}")
                }
            }
        } catch (e: Exception) {
            val errorHtml =
                """
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
                    <p>File: $fileName (ReadType: $readType)</p>
                    <p>Attempted Path: $attemptedResourcePath</p>
                    <p>Error: ${e::class.simpleName} - ${e.message}</p>
                    ${e.cause?.let { "<p>Cause: ${it.message}</p>" } ?: ""}
                    <pre>${e.stackTraceToString()}</pre>
                </body>
                </html>
                """.trimIndent()
            delay(200)
            webView.loadHtml(errorHtml)
            KLogger.e(e) { "DesktopWebView loadHtmlFile error for $fileName (ReadType: $readType)" }
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
    private fun inlineExternalResources(
        htmlContent: String,
        basePathInJar: String,
    ): String {
        var modifiedHtml = htmlContent

        try {
            // Inline CSS files
            val cssPattern =
                """<link\s+[^>]*href\s*=\s*["']([^"']+\.css)["'][^>]*>"""
                    .toRegex(RegexOption.IGNORE_CASE)
            modifiedHtml =
                cssPattern.replace(modifiedHtml) { matchResult ->
                    val cssFile = matchResult.groupValues[1]
                    try {
                        val resourcePath = "$basePathInJar$cssFile".removePrefix("/")
                        val cssInputStream =
                            this::class.java.classLoader.getResourceAsStream(resourcePath)
                        if (cssInputStream != null) {
                            val cssContent =
                                InputStreamReader(
                                    cssInputStream,
                                    StandardCharsets.UTF_8,
                                ).use { it.readText() }
                            "<style>\n$cssContent\n</style>"
                        } else {
                            KLogger.e { "DesktopWebView: CSS resource not found for inlining: $resourcePath" }
                            matchResult.value // Keep original if can't inline
                        }
                    } catch (e: Exception) {
                        KLogger.e { "DesktopWebView: Could not inline CSS $cssFile: ${e.message}" }
                        matchResult.value
                    }
                }

            // Inline JS files
            val jsPattern =
                """<script\s+[^>]*src\s*=\s*["']([^"']+\.js)["'][^>]*></script>"""
                    .toRegex(RegexOption.IGNORE_CASE)
            modifiedHtml =
                jsPattern.replace(modifiedHtml) { matchResult ->
                    val jsFile = matchResult.groupValues[1]
                    try {
                        val resourcePath = "$basePathInJar$jsFile".removePrefix("/")
                        val jsInputStream =
                            this::class.java.classLoader.getResourceAsStream(resourcePath)
                        if (jsInputStream != null) {
                            val jsContent =
                                InputStreamReader(
                                    jsInputStream,
                                    StandardCharsets.UTF_8,
                                ).use { it.readText() }
                            "<script>\n$jsContent\n</script>"
                        } else {
                            KLogger.e { "DesktopWebView: JS resource not found for inlining: $resourcePath" }
                            matchResult.value // Keep original if can't inline
                        }
                    } catch (e: Exception) {
                        KLogger.e { "DesktopWebView: Could not inline JS $jsFile: ${e.message}" }
                        matchResult.value
                    }
                }
        } catch (e: Exception) {
            KLogger.e { "DesktopWebView: Error during resource inlining: ${e.message}" }
        }

        return modifiedHtml
    }

    override fun saveState(): WebViewBundle? = null

    override fun scrollOffset(): Pair<Int, Int> = Pair(0, 0)
}
