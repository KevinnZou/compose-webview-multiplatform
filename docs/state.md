# State

This library provides
a [WebViewState](https://github.com/KevinnZou/compose-webview-multiplatform/blob/main/webview/src/commonMain/kotlin/com/multiplatform/webview/web/WebViewState.kt)
class as a state holder to hold the state for the WebView.

## WebViewState

```kotlin
class WebViewState(webContent: WebContent) {
    var lastLoadedUrl: String? by mutableStateOf(null)
        internal set

    /**
     *  The content being loaded by the WebView
     */
    var content: WebContent by mutableStateOf(webContent)

    /**
     * Whether the WebView is currently [LoadingState.Loading] data in its main frame (along with
     * progress) or the data loading has [LoadingState.Finished]. See [LoadingState]
     */
    var loadingState: LoadingState by mutableStateOf(LoadingState.Initializing)
        internal set

    /**
     * Whether the webview is currently loading data in its main frame
     */
    val isLoading: Boolean
        get() = loadingState !is LoadingState.Finished

    /**
     * The title received from the loaded content of the current page
     */
    var pageTitle: String? by mutableStateOf(null)
        internal set

    /**
     * A list for errors captured in the last load. Reset when a new page is loaded.
     * Errors could be from any resource (iframe, image, etc.), not just for the main page.
     * For more fine grained control use the OnError callback of the WebView.
     */
    val errorsForCurrentRequest: SnapshotStateList<WebViewError> = mutableStateListOf()

    /**
     * Custom Settings for WebView.
     */
    val webSettings: WebSettings by mutableStateOf(WebSettings())

    // We need access to this in the state saver. An internal DisposableEffect or AndroidView
    // onDestroy is called after the state saver and so can't be used.
    internal var webView by mutableStateOf<IWebView?>(null)
}
```

## rememberWebViewState

It can be created using
the [rememberWebViewState](https://github.com/KevinnZou/compose-webview-multiplatform/blob/c1104c4458277423ec0ee3386140e06950483cb4/webview/src/commonMain/kotlin/com/multiplatform/webview/web/WebViewState.kt#L87)
function, which can be remembered across Compositions.

```kotlin
val state = rememberWebViewState("https://github.com/KevinnZou/compose-webview-multiplatform")

/**
 * Creates a WebView state that is remembered across Compositions.
 *
 * @param url The url to load in the WebView
 * @param additionalHttpHeaders Optional, additional HTTP headers that are passed to [WebView.loadUrl].
 *                              Note that these headers are used for all subsequent requests of the WebView.
 */
@Composable
fun rememberWebViewState(
    url: String,
    additionalHttpHeaders: Map<String, String> = emptyMap()
)

/**
 * Creates a WebView state that is remembered across Compositions.
 *
 * @param data The uri to load in the WebView
 * @param baseUrl The URL to use as the page's base URL.
 * @param encoding The encoding of the data in the string.
 * @param mimeType The MIME type of the data in the string.
 * @param historyUrl The history URL for the loaded HTML. Leave null to use about:blank.
 */
@Composable
fun rememberWebViewStateWithHTMLData(
    data: String,
    baseUrl: String? = null,
    encoding: String = "utf-8",
    mimeType: String? = null,
    historyUrl: String? = null
)

/**
 * Creates a WebView state that is remembered across Compositions.
 *
 * @param fileName The file to load in the WebView
 */
@Composable
fun rememberWebViewStateWithHTMLFile(
    fileName: String,
)
```

## rememberSaveableWebViewState

This library also provides a `rememberSaveableWebViewState` function that can be used to create a
`WebViewState` that is remembered across recompositions.

It can be used in tab layouts, where the state of the WebView should be saved when the user switches
between tabs.

```kotlin
@Composable
fun Home() {
    val url = "https://www.jetbrains.com/lp/compose-multiplatform/"
    val webViewState =
        rememberSaveableWebViewState(url).apply {
            webSettings.logSeverity = KLogSeverity.Debug
        }

    val navigator = rememberWebViewNavigator()

    LaunchedEffect(navigator) {
        val bundle = webViewState.viewState
        if (bundle == null) {
            // This is the first time load, so load the home page.
            navigator.loadUrl(url)
        }
    }

    WebView(
        state = webViewState,
        modifier = Modifier.fillMaxSize().padding(bottom = 45.dp),
        navigator = navigator,
    )
}
```

Please refer to
the [VoyagerNavigationSample](https://github.com/KevinnZou/compose-webview-multiplatform/blob/main/sample/shared/src/commonMain/kotlin/com/kevinnzou/sample/VoyagerNavigationSample.kt)
for whole example.

## Usage

Developers can use the `WebViewState` to get the loading information of the WebView, such as the
loading progress, the loading status, and the URL of the current page.

```kotlin
Column {
    val state = rememberWebViewState("https://github.com/KevinnZou/compose-webview-multiplatform")

    Text(text = "${state.pageTitle}")
    val loadingState = state.loadingState
    if (loadingState is LoadingState.Loading) {
        LinearProgressIndicator(
            progress = loadingState.progress,
            modifier = Modifier.fillMaxWidth()
        )
    }
    WebView(
        state
    )
}
```