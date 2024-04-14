package com.multiplatform.webview.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.w3c.dom.Element
import org.w3c.dom.HTMLFormElement
import org.w3c.dom.HTMLIFrameElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.Node

/**
 * Wasm WebView implementation.
 */
@Composable
actual fun ActualWebView(
    state: WebViewState,
    modifier: Modifier,
    captureBackPresses: Boolean,
    navigator: WebViewNavigator,
    webViewJsBridge: WebViewJsBridge?,
    onCreated: () -> Unit,
    onDispose: () -> Unit,
) {
    WasmWebView(
        state,
        modifier,
        navigator,
        webViewJsBridge,
        onCreated = onCreated,
        onDispose = onDispose,
    )
}

/**
 * Wasm WebView implementation.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
fun WasmWebView(
    state: WebViewState,
    modifier: Modifier,
    navigator: WebViewNavigator,
    webViewJsBridge: WebViewJsBridge?,
    onCreated: () -> Unit,
    onDispose: () -> Unit,
) {
    val webView = state.webView
    val scope = rememberCoroutineScope()
    val iframe = document.createElement("iframe") as HTMLIFrameElement
    iframe.style.width = "100%"
    iframe.style.height = "100%"
    document.body?.insertBefore(iframe, document.body?.firstChild)
    WasmWebView(iframe, state, scope, webViewJsBridge)

}

class WasmWebView(
    private val iframe: HTMLIFrameElement,
    private val state: WebViewState,
    override val scope: CoroutineScope,
    override val webViewJsBridge: WebViewJsBridge?,
) : IWebView {
    init {
        initWebView()
        iframe.src = state.lastLoadedUrl?: "about:blank"
        state.loadingState = LoadingState.Finished
    }

    override fun canGoBack(): Boolean {
        val historyLength = iframe.contentWindow?.history?.length
        return historyLength != null && historyLength > 1
    }

    override fun canGoForward(): Boolean {
        TODO("Not yet implemented")
    }

    override fun loadUrl(url: String, additionalHttpHeaders: Map<String, String>) {
        iframe.src = url
    }

    override fun loadHtml(
        html: String?,
        baseUrl: String?,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?
    ) {
        iframe.let {
            val content = if (!baseUrl.isNullOrEmpty()) {
                "<base href=\"$baseUrl\">$html"
            } else {
                html ?: ""
            }
            it.srcdoc = content
        }
    }

    override suspend fun loadHtmlFile(fileName: String) {
        TODO("Not yet implemented")
    }

    override fun postUrl(url: String, postData: ByteArray) {
        val base64Data = window.btoa(postData.decodeToString())
        val form = iframe.contentDocument?.createElement("form") as? HTMLFormElement
        form?.apply {
            action = url
            method = "post"
            target = "_self"

            val input = iframe.contentDocument?.createElement("input") as? HTMLInputElement
            input?.apply {
                type = "hidden"
                name = "data"
                value = base64Data
            }
            this.appendChild(input as Node)

            iframe.contentDocument?.body?.appendChild(this)
            submit()
        }
    }

    override fun goBack() {
        iframe.contentWindow?.history?.back() ?: run {
            println("No history to go back to, or iframe/contentWindow is not accessible")
        }
    }

    override fun goForward() {
        iframe.contentWindow?.history?.forward() ?: run {
            println("No history to go forward to, or iframe/contentWindow is not accessible")
        }
    }

    override fun reload() {
        iframe.src = iframe.src
    }

    override fun stopLoading() {
        iframe.src = "about:blank"
        state.loadingState = LoadingState.Finished
    }

    override fun evaluateJavaScript(script: String, callback: ((String) -> Unit)?) {
        TODO("Not yet implemented")
    }

    override fun initJsBridge(webViewJsBridge: WebViewJsBridge) {
        if (webViewJsBridge == null) return
        super.injectJsBridge()
        val call =
            """
            window.${webViewJsBridge.jsBridgeName}.postMessage = function (message) {
                    window.androidJsBridge.call(message)
                };
            """.trimIndent()
        evaluateJavaScript(call)
    }
}
