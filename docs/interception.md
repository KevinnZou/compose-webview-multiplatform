# Request Interception

Starting from version 1.9.8, this library provides a `RequestInterceptor` to allow developers to
intercept the request and modify the request headers. It only supports the Android and iOS platform
for now.

## RequestInterceptor

```kotlin
/**
 * Interface for intercepting requests in WebView.
 */
interface RequestInterceptor {
    fun onInterceptUrlRequest(
        request: WebRequest,
        navigator: WebViewNavigator,
    ): WebRequestInterceptResult
}
```

The `onInterceptUrlRequest` method will be called when the WebView sends a request.

## Sample

Developers can implement the `RequestInterceptor` interface to define their own interceptor.
Then they can pass it to the `rememberWebViewNavigator` method to intercept the request.

```kotlin
val navigator =
    rememberWebViewNavigator(
        requestInterceptor =
        object : RequestInterceptor {
            override fun onInterceptUrlRequest(
                request: WebRequest,
                navigator: WebViewNavigator,
            ): WebRequestInterceptResult {
                return if (request.url.contains("kotlin")) {
                    WebRequestInterceptResult.Modify(
                        WebRequest(
                            url = "https://kotlinlang.org/docs/multiplatform.html",
                            headers = mutableMapOf("info" to "test"),
                        ),
                    )
                } else {
                    WebRequestInterceptResult.Allow
                }
            }
        },
    )
```