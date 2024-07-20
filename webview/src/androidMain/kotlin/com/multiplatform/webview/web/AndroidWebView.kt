package com.multiplatform.webview.web

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View.MeasureSpec
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.view.drawToBitmap
import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import com.multiplatform.webview.util.KLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json

/**
 * Created By Kevin Zou On 2023/9/5
 */

actual typealias NativeWebView = WebView

/**
 * Android implementation of [IWebView]
 */
class AndroidWebView(
    override val webView: WebView,
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
        webView.loadUrl(url, additionalHttpHeaders)
    }

    override fun loadHtml(
        html: String?,
        baseUrl: String?,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?,
    ) {
        if (html == null) return
        webView.loadDataWithBaseURL(baseUrl, html, mimeType, encoding, historyUrl)
    }

    override suspend fun loadHtmlFile(fileName: String) {
        KLogger.d {
            "loadHtmlFile: $fileName"
        }
        webView.loadUrl("file:///android_asset/$fileName")
    }

    override fun postUrl(
        url: String,
        postData: ByteArray,
    ) {
        webView.postUrl(url, postData)
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
        val androidScript = "javascript:$script"
        KLogger.d {
            "evaluateJavaScript: $androidScript"
        }
        webView.post {
            webView.evaluateJavascript(androidScript, callback)
        }
    }

    override fun injectJsBridge() {
        if (webViewJsBridge == null) return
        super.injectJsBridge()
        val callAndroid =
            """
            window.${webViewJsBridge.jsBridgeName}.postMessage = function (message) {
                    window.androidJsBridge.call(message)
                };
            """.trimIndent()
        evaluateJavaScript(callAndroid)
    }

    override fun initJsBridge(webViewJsBridge: WebViewJsBridge) {
        webView.addJavascriptInterface(this, "androidJsBridge")
    }

    @JavascriptInterface
    fun call(request: String) {
        KLogger.d { "call from JS: $request" }
        val message = Json.decodeFromString<JsMessage>(request)
        KLogger.d {
            "call from JS: $message"
        }
        webViewJsBridge?.dispatch(message)
    }

    @JavascriptInterface
    fun callAndroid(
        id: Int,
        method: String,
        params: String,
    ) {
        KLogger.d { "callAndroid call from JS: $id, $method, $params" }
        webViewJsBridge?.dispatch(JsMessage(id, method, params))
    }

    override fun scrollOffset(): Pair<Int, Int> {
        return Pair(webView.scrollX, webView.scrollY)
    }

    override fun saveState(): WebViewBundle? {
        val bundle = WebViewBundle()
        return if (webView.saveState(bundle) != null) {
            bundle
        } else {
            null
        }
    }

    override fun takeSnapshot(rect: Rect?, completionHandler: (ImageBitmap?) -> Unit) {
        try {
            // Measure the WebView
            webView.measure(
                MeasureSpec.makeMeasureSpec(
                    MeasureSpec.UNSPECIFIED,
                    MeasureSpec.UNSPECIFIED
                ),
                MeasureSpec.makeMeasureSpec(
                    0,
                    MeasureSpec.UNSPECIFIED
                )
            )
            val left = rect?.left?.toInt() ?: 0
            val top = rect?.top?.toInt() ?: 0
            val right = rect?.right?.toInt() ?: webView.measuredWidth
            val bottom = rect?.bottom?.toInt() ?: webView.measuredHeight

            webView.layout(
                0,
                0,
                right,
                bottom
            )

            // Create a bitmap with the appropriate dimensions
            val bm = Bitmap.createBitmap(
                rect?.width?.toInt() ?: right,
                rect?.height?.toInt() ?: bottom,
                Bitmap.Config.ARGB_8888
            )

            // Create a canvas to draw the WebView
            val canvas = Canvas(bm)

            // Translate the canvas to account for the scroll position and
            // the specified rectangle
            if (rect != null) {
                // Get the current scroll position
                val scrollX = webView.scrollX
                val scrollY = webView.scrollY
                canvas.translate((-scrollX).toFloat() - left, (-scrollY).toFloat() - top)
            }
            webView.draw(canvas)
            completionHandler.invoke(bm.asImageBitmap())
        } catch (e: Exception) {
            completionHandler.invoke(null)
        }
    }
}
