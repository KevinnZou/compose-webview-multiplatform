# Navigation
This library provides a [WebViewNavigator](https://github.com/KevinnZou/compose-webview-multiplatform/blob/main/webview/src/commonMain/kotlin/com/multiplatform/webview/web/WebViewNavigator.kt) class 
to control over the navigation of a WebView from outside the composable. 

E.g.for performing a back navigation in response to the user clicking the "up" button in a TopAppBar.

It can be used to load a new URL, evaluate the JavaScript, and go back and forward in the history.

## WebViewNavigator
```kotlin
class WebViewNavigator(private val coroutineScope: CoroutineScope) {

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

    fun loadUrl(url: String, additionalHttpHeaders: Map<String, String> = emptyMap()) {}

    fun loadHtml(
        html: String,
        baseUrl: String? = null,
        mimeType: String? = null,
        encoding: String? = "utf-8",
        historyUrl: String? = null
    ) {
    }

    fun postUrl(
        url: String,
        postData: ByteArray
    ) {
    }

    fun evaluateJavaScript(script: String, callback: ((String) -> Unit)? = null)

    /**
     * Navigates the webview back to the previous page.
     */
    fun navigateBack() {}

    /**
     * Navigates the webview forward after going back from a page.
     */
    fun navigateForward() {}

    /**
     * Reloads the current page in the webview.
     */
    fun reload() {}

    /**
     * Stops the current page load (if one is loading).
     */
    fun stopLoading() {}
}
```

## Creating a WebViewNavigator
It can be created using the [rememberWebViewNavigator](https://github.com/KevinnZou/compose-webview-multiplatform/blob/c1104c4458277423ec0ee3386140e06950483cb4/webview/src/commonMain/kotlin/com/multiplatform/webview/web/WebViewNavigator.kt#L301) function, which can be remembered across Compositions.

```kotlin
val navigator = rememberWebViewNavigator()

@Composable
fun rememberWebViewNavigator(
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): WebViewNavigator = remember(coroutineScope) { WebViewNavigator(coroutineScope) }
```

## Usage
Developers can use the *WebViewNavigator* to control the navigation of the WebView.
```kotlin
val navigator = rememberWebViewNavigator()

Column {
    val state = rememberWebViewState("https://example.com")
    val navigator = rememberWebViewNavigator()

    TopAppBar(
        title = { Text(text = "WebView Sample") },
        navigationIcon = {
            if (navigator.canGoBack) {
                IconButton(onClick = { navigator.navigateBack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        }
    )
    Text(text = "${state.pageTitle}")
    val loadingState = state.loadingState
    if (loadingState is LoadingState.Loading) {
        LinearProgressIndicator(
          progress = loadingState.progress,
          modifier = Modifier.fillMaxWidth()
        )
    }
  WebView(
    state = state,
    navigator = navigator
  )
}
```
