package com.multiplatform.webview.web

import com.multiplatform.webview.jsbridge.WKJsMessageHandler
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import com.multiplatform.webview.util.KLogger
import com.multiplatform.webview.util.getPlatformVersionDouble
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CoroutineScope
import platform.Foundation.HTTPBody
import platform.Foundation.HTTPMethod
import platform.Foundation.NSBundle
import platform.Foundation.NSData
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.setValue
import platform.Foundation.stringByDeletingLastPathComponent
import platform.WebKit.WKWebView
import platform.darwin.NSObject
import platform.darwin.NSObjectMeta

/**
 * Created By Kevin Zou On 2023/9/5
 */

actual typealias NativeWebView = WKWebView

/**
 * iOS implementation of [IWebView]
 */
class IOSWebView(
    override val webView: WKWebView,
    override val scope: CoroutineScope,
    override val webViewJsBridge: WebViewJsBridge?,
) : IWebView {
    init {
        initWebView()
    }

    override fun canGoBack() = webView.canGoBack

    override fun canGoForward() = webView.canGoForward

    override fun loadUrl(
        url: String,
        additionalHttpHeaders: Map<String, String>,
    ) {
        val request =
            NSMutableURLRequest.requestWithURL(
                URL = NSURL(string = url),
            )
        additionalHttpHeaders.all { (key, value) ->
            request.setValue(
                value = value,
                forHTTPHeaderField = key,
            )
            true
        }
        webView.loadRequest(
            request = request,
        )
    }

    override suspend fun loadHtml(
        html: String?,
        baseUrl: String?,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?,
    ) {
        if (html == null) {
            KLogger.e {
                "LoadHtml: html is null"
            }
            return
        }
        webView.loadHTMLString(
            string = html,
            baseURL = baseUrl?.let { NSURL.URLWithString(it) },
        )
    }

    override suspend fun loadHtmlFile(
        fileName: String,
        readType: WebViewFileReadType,
    ) {
        try {
            val fileURL: NSURL
            var readAccessURL: NSURL? = null

            when (readType) {
                WebViewFileReadType.ASSET_RESOURCES -> {
                    val resourcePath =
                        (NSBundle.mainBundle.resourcePath ?: "") +
                            "/compose-resources/assets/" + fileName
                    fileURL = NSURL.fileURLWithPath(resourcePath)

                    val parentDir = (resourcePath as NSString).stringByDeletingLastPathComponent()
                    if (parentDir.isNotBlank()) {
                        readAccessURL = NSURL.fileURLWithPath(parentDir)
                    } else {
                        readAccessURL = NSURL.fileURLWithPath(NSBundle.mainBundle.resourcePath!!)
                    }
                }

                WebViewFileReadType.COMPOSE_RESOURCE_FILES -> {
                    fileURL = NSURL(string = fileName)
                    val readAccessURLPath =
                        (fileName as NSString).stringByDeletingLastPathComponent()
                    readAccessURL = NSURL(string = readAccessURLPath)
                }
            }

            if (!fileURL.isFileURL()) {
                KLogger.e {
                    "The determined fileURL is not a valid file URL: ${fileURL.absoluteString}"
                }
                loadHtml(
                    "<html><body>Error: Not a file URL: ${fileURL.absoluteString}</body></html>",
                )
                return
            }

            val finalReadAccessURL = readAccessURL

            if (finalReadAccessURL.path.isNullOrEmpty()) {
                KLogger.e {
                    "Critical: finalReadAccessURL is null or has an empty path. " +
                        "Cannot load file with proper read access for ${fileURL.absoluteString}"
                }
                loadHtml(
                    "<html><body>Error: Cannot determine read access URL " +
                        "for ${fileURL.absoluteString}</body></html>",
                )
                return
            }

            webView.loadFileURL(fileURL, finalReadAccessURL)
        } catch (e: Exception) {
            KLogger.e(e) { "Error loading HTML file: $fileName (readType: $readType)" }
            val errorHtml =
                """
                <!DOCTYPE html>
                <html><head><title>Error</title></head>
                <body>
                    <h1>Error Loading File</h1>
                    <p>Could not load: $fileName (readType: $readType)</p>
                    <p>Error: ${e.message}</p>
                </body></html>
                """.trimIndent()
            loadHtml(errorHtml)
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override fun postUrl(
        url: String,
        postData: ByteArray,
    ) {
        val request =
            NSMutableURLRequest(
                uRL = NSURL(string = url),
            )
        request.apply {
            HTTPMethod = "POST"
            HTTPBody =
                memScoped {
                    NSData.create(bytes = allocArrayOf(postData), length = postData.size.toULong())
                }
        }
        webView.loadRequest(request = request)
    }

    override fun goBack() {
        webView.goBack()
    }

    override fun goForward() {
        webView.goForward()
    }

    override fun reload() {
        webView.reload()
    }

    override fun stopLoading() {
        webView.stopLoading()
    }

    override fun evaluateJavaScript(
        script: String,
        callback: ((String) -> Unit)?,
    ) {
        webView.evaluateJavaScript(script) { result, error ->
            if (callback == null) return@evaluateJavaScript
            if (error != null) {
                KLogger.e { "evaluateJavaScript error: $error" }
                callback.invoke(error.localizedDescription())
            } else {
                callback.invoke(result?.toString() ?: "")
            }
        }
    }

    override fun injectJsBridge() {
        if (webViewJsBridge == null) return
        super.injectJsBridge()
        val callIOS =
            """
            window.${webViewJsBridge.jsBridgeName}.postMessage = function (message) {
                    window.webkit.messageHandlers.iosJsBridge.postMessage(message);
                };
            """.trimIndent()
        evaluateJavaScript(callIOS)
    }

    override fun initJsBridge(webViewJsBridge: WebViewJsBridge) {
        val jsMessageHandler = WKJsMessageHandler(webViewJsBridge)
        webView.configuration.userContentController.apply {
            addScriptMessageHandler(jsMessageHandler, "iosJsBridge")
        }
    }

    override fun saveState(): WebViewBundle? {
        // iOS 15- does not support saving state
        if (getPlatformVersionDouble() < 15.0) {
            return null
        }
        val data = webView.interactionState as NSData?
        return data
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun scrollOffset(): Pair<Int, Int> {
        val offset = webView.scrollView.contentOffset
        offset.useContents {
            return Pair(x.toInt(), y.toInt())
        }
    }

    private class BundleMarker : NSObject() {
        companion object : NSObjectMeta()
    }
}
