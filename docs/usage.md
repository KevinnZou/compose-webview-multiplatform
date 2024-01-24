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
