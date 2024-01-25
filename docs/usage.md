# Basic Usage

## Loading URLs
To use this widget there are two key APIs that are needed: *WebView*, which provides the layout, and *rememberWebViewState(url)* which provides some remembered state including the URL to display.

The basic usage is as follows:
```kotlin
val state = rememberWebViewState("https://example.com")

WebView(state)
```
This will display a WebView in your Compose layout that shows the URL provided.


## Loading HTML

This library supports loading HTML data and HTML files.

### HTML Data
Developers can load HTML data in the following way:
```kotlin
val html = """
    <html>
        <body>
            <h1>Hello World</h1>
        </body>
    </html>
""".trimIndent()

val webViewState = rememberWebViewStateWithHTMLData(
    data = html
)

WebView(state)
```

### HTML File
Developers can load HTML files in the following way:
```kotlin
val webViewState = rememberWebViewStateWithHTMLFile(
    fileName = "index.html"
)

WebView(state)
```
Note that the HTML file should be put in the `resources/assets` folder of the shared module.

It also supports external resources such as images, CSS, and JavaScript files on Android and iOS. Desktop support is coming soon.


## Sample
A more complete sample is available in the [BasicWebViewSample](https://github.com/KevinnZou/compose-webview-multiplatform/blob/main/sample/shared/src/commonMain/kotlin/com/kevinnzou/sample/BasicWebViewSample.kt)

```kotlin
@Composable
internal fun BasicWebViewSample() {
    val initialUrl = "https://github.com/KevinnZou/compose-webview-multiplatform"
    val state = rememberWebViewState(url = initialUrl)
    LaunchedEffect(Unit) {
        state.webSettings.apply {
            logSeverity = KLogSeverity.Debug
            customUserAgentString =
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_1) AppleWebKit/625.20 (KHTML, like Gecko) Version/14.3.43 Safari/625.20"
        }
    }
    val navigator = rememberWebViewNavigator()
    var textFieldValue by remember(state.lastLoadedUrl) {
        mutableStateOf(state.lastLoadedUrl)
    }
    MaterialTheme {
        Column {
            TopAppBar(
                title = { Text(text = "WebView Sample") },
                navigationIcon = {
                    if (navigator.canGoBack) {
                        IconButton(onClick = { navigator.navigateBack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }
                },
            )

            Row {
                Box(modifier = Modifier.weight(1f)) {
                    if (state.errorsForCurrentRequest.isNotEmpty()) {
                        Image(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Error",
                            colorFilter = ColorFilter.tint(Color.Red),
                            modifier =
                                Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(8.dp),
                        )
                    }

                    OutlinedTextField(
                        value = textFieldValue ?: "",
                        onValueChange = { textFieldValue = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Button(
                    onClick = {
                        textFieldValue?.let {
                            navigator.loadUrl(it)
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically),
                ) {
                    Text("Go")
                }
            }

            val loadingState = state.loadingState
            if (loadingState is LoadingState.Loading) {
                LinearProgressIndicator(
                    progress = loadingState.progress,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            WebView(
                state = state,
                modifier =
                    Modifier
                        .fillMaxSize(),
                navigator = navigator,
            )
        }
    }
}
```