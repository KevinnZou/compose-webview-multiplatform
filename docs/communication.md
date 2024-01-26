# Communication between WebView and Native

## Native to WebView
Starting from version 1.2.0, this library provides the `evaluateJavaScript` method to allow developers to
send messages from the Native to WebView.

```kotlin
/**
 * Evaluates the given JavaScript in the context of the currently displayed page.
 *
 * @param script The JavaScript to evaluate.
 * @param callback A callback to be invoked when the script execution completes.
 */
fun evaluateJavaScript(
    script: String,
    callback: ((String) -> Unit)? = null,
)
```

Developers can use it like that:
```kotlin
val state = rememberWebViewState("https://example.com")
val webViewNavigator = rememberWebViewNavigator()
MaterialTheme {
    Box(Modifier.fillMaxSize()) {
        WebView(
            state = webViewState,
            modifier = Modifier.fillMaxSize(),
            captureBackPresses = false,
            navigator = webViewNavigator,
            webViewJsBridge = jsBridge,
        )
        Button(
            onClick = {
                webViewNavigator.evaluateJavaScript(
                    """
                    document.getElementById("subtitle").innerText = "Hello from KMM!";
                    """.trimIndent(),
                ) {
                    // handle the result
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 50.dp),
        ) {
            Text(jsRes)
        }
    }
}
```


## WebView to Native

Starting from version 1.8.0, this library provides a [WebViewJsBridge](https://github.com/KevinnZou/compose-webview-multiplatform/blob/main/webview/src/commonMain/kotlin/com/multiplatform/webview/jsbridge/WebViewJsBridge.kt) 
to allow developers to send messages from the WebView to Native.

Developers can use the `WebViewJsBridge` to register a handler to handle the message from the WebView.

```kotlin
val jsBridge = rememberWebViewJsBridge()

LaunchedEffect(jsBridge) {
  jsBridge.register(GreetJsMessageHandler())
}
```

The handler should implement the `IJsMessageHandler` interface.

```kotlin
interface IJsMessageHandler {
  fun methodName(): String

  fun canHandle(methodName: String) = methodName() == methodName

  fun handle(
    message: JsMessage,
    callback: (String) -> Unit,
  )

}

class GreetJsMessageHandler : IJsMessageHandler {
  override fun methodName(): String {
    return "Greet"
  }

  override fun handle(message: JsMessage, callback: (String) -> Unit) {
    Logger.i {
      "Greet Handler Get Message: $message"
    }
    val param = processParams<GreetModel>(message)
    val data = GreetModel("KMM Received ${param.message}")
    callback(dataToJsonString(data))
  }
}
```

Developers can use the `window.kmpJsBridge.callNative` to send a message to the Native.
It receives three parameters:

* methodName: the name of the handler registered in the Native.
* params: the parameters to send to the Native. It needs to be a JSON string.
* callback: the callback function to handle the response from the Native. It receives a JSON string
  as the parameter. Pass null if no callback is needed.

```javascript
window.kmpJsBridge.callNative = function (methodName, params, callback) {}

window.kmpJsBridge.callNative("Greet",JSON.stringify({message:"Hello"}),
  function (data) {
    document.getElementById("subtitle").innerText = data;
    console.log("Greet from Native: " + data);
  }
);
```

**Note:** Starting from version 1.8.6, the name of the JsBridge is configurable. Developers can configure it in the `rememberWebViewJsBridge` method.
This library uses the `kmpJsBridge` as the default.