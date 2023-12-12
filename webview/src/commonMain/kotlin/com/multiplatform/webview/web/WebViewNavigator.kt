package com.multiplatform.webview.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created By Kevin Zou On 2023/9/5
 */

/**
 * Allows control over the navigation of a WebView from outside the composable. E.g. for performing
 * a back navigation in response to the user clicking the "up" button in a TopAppBar.
 *
 * @see [rememberWebViewNavigator]
 */
@Stable
class WebViewNavigator(private val coroutineScope: CoroutineScope) {
    /**
     * Sealed class for constraining possible navigation events.
     */
    private sealed interface NavigationEvent {
        /**
         * Navigate back event.
         */
        data object Back : NavigationEvent

        /**
         * Navigate forward event.
         */
        data object Forward : NavigationEvent

        /**
         * Reload event.
         */
        data object Reload : NavigationEvent

        /**
         * Stop loading event.
         */
        data object StopLoading : NavigationEvent

        /**
         * Load url event.
         */
        data class LoadUrl(
            val url: String,
            val additionalHttpHeaders: Map<String, String> = emptyMap(),
        ) : NavigationEvent

        /**
         * Load html event.
         */
        data class LoadHtml(
            val html: String,
            val baseUrl: String? = null,
            val mimeType: String? = null,
            val encoding: String? = "utf-8",
            val historyUrl: String? = null,
        ) : NavigationEvent

        data class LoadHtmlFile(
            val fileName: String,
        ) : NavigationEvent

        /**
         * Post url event.
         */
        data class PostUrl(
            val url: String,
            val postData: ByteArray,
        ) : NavigationEvent {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || this::class != other::class) return false

                other as PostUrl

                if (url != other.url) return false
                if (!postData.contentEquals(other.postData)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = url.hashCode()
                result = 31 * result + postData.contentHashCode()
                return result
            }
        }

        /**
         * Evaluate javascript event.
         */
        data class EvaluateJavaScript(
            val script: String,
            val callback: ((String) -> Unit)?,
        ) : NavigationEvent
    }

    /**
     * A [MutableSharedFlow] of [NavigationEvent]s that is used to communicate navigation events
     * from the composable to the [IWebView].
     */
    private val navigationEvents: MutableSharedFlow<NavigationEvent> = MutableSharedFlow(replay = 1)

    /**
     * Handles navigation events from the composable and calls the appropriate method on the
     * [IWebView].
     * Use Dispatchers.Main to ensure that the webview methods are called on UI thread
     */
    internal suspend fun IWebView.handleNavigationEvents(): Nothing =
        withContext(Dispatchers.Main) {
            navigationEvents.collect { event ->
                when (event) {
                    is NavigationEvent.Back -> goBack()
                    is NavigationEvent.Forward -> goForward()
                    is NavigationEvent.Reload -> reload()
                    is NavigationEvent.StopLoading -> stopLoading()
                    is NavigationEvent.LoadHtml ->
                        loadHtml(
                            event.html,
                            event.baseUrl,
                            event.mimeType,
                            event.encoding,
                            event.historyUrl,
                        )

                    is NavigationEvent.LoadHtmlFile -> {
                        loadHtmlFile(event.fileName)
                    }

                    is NavigationEvent.LoadUrl -> {
                        loadUrl(event.url, event.additionalHttpHeaders)
                    }

                    is NavigationEvent.PostUrl -> {
                        postUrl(event.url, event.postData)
                    }

                    is NavigationEvent.EvaluateJavaScript -> {
                        evaluateJavaScript(event.script, event.callback)
                    }
                }
            }
        }

    /**
     * True when the web view is able to navigate backwards, false otherwise.
     */
    var canGoBack: Boolean by mutableStateOf(false)
        internal set

    /**
     * True when the web view is able to navigate forwards, false otherwise.
     */
    var canGoForward: Boolean by mutableStateOf(false)
        internal set

    /**
     * Loads the given URL.
     *
     * @param url The URL of the resource to load.
     */
    fun loadUrl(
        url: String,
        additionalHttpHeaders: Map<String, String> = emptyMap(),
    ) {
        coroutineScope.launch {
            navigationEvents.emit(
                NavigationEvent.LoadUrl(
                    url,
                    additionalHttpHeaders,
                ),
            )
        }
    }

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
        html: String,
        baseUrl: String? = null,
        mimeType: String? = null,
        encoding: String? = "utf-8",
        historyUrl: String? = null,
    ) {
        coroutineScope.launch {
            navigationEvents.emit(
                NavigationEvent.LoadHtml(
                    html,
                    baseUrl,
                    mimeType,
                    encoding,
                    historyUrl,
                ),
            )
        }
    }

    fun loadHtmlFile(fileName: String) {
        coroutineScope.launch {
            navigationEvents.emit(
                NavigationEvent.LoadHtmlFile(
                    fileName,
                ),
            )
        }
    }

    /**
     * Posts the given data to the given URL.
     *
     * @param url The URL to post the data to.
     * @param postData The data to post.
     */
    fun postUrl(
        url: String,
        postData: ByteArray,
    ) {
        coroutineScope.launch {
            navigationEvents.emit(
                NavigationEvent.PostUrl(
                    url,
                    postData,
                ),
            )
        }
    }

    /**
     * Evaluates the given JavaScript in the context of the currently displayed page.
     *
     * @param script The JavaScript to evaluate.
     * @param callback A callback to be invoked when the script execution completes.
     */
    fun evaluateJavaScript(
        script: String,
        callback: ((String) -> Unit)? = null,
    ) {
        coroutineScope.launch {
            navigationEvents.emit(
                NavigationEvent.EvaluateJavaScript(
                    script,
                    callback,
                ),
            )
        }
    }

    /**
     * Navigates the webview back to the previous page.
     */
    fun navigateBack() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Back) }
    }

    /**
     * Navigates the webview forward after going back from a page.
     */
    fun navigateForward() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Forward) }
    }

    /**
     * Reloads the current page in the webview.
     */
    fun reload() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.Reload) }
    }

    /**
     * Stops the current page load (if one is loading).
     */
    fun stopLoading() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.StopLoading) }
    }
}

/**
 * Creates and remembers a [WebViewNavigator] using the default [CoroutineScope] or a provided
 * override.
 */
@Composable
fun rememberWebViewNavigator(coroutineScope: CoroutineScope = rememberCoroutineScope()): WebViewNavigator =
    remember(coroutineScope) { WebViewNavigator(coroutineScope) }
