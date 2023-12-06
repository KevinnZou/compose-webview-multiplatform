package com.kevinnzou.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.kevinnzou.sample.model.GreetModel
import com.multiplatform.webview.jsbridge.IJsHandler
import com.multiplatform.webview.jsbridge.JsMessage
import com.multiplatform.webview.jsbridge.processParams
import com.multiplatform.webview.util.KLogSeverity
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLData

/**
 * Created By Kevin Zou On 2023/9/8
 */
@Composable
internal fun BasicWebViewWithHTMLSample() {
    val html =
        """
        <html>
        <head>
            <title>Compose WebView Multiplatform</title>
            <style>
                body {
                    background-color: e0e8f0; 
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    flex-direction: column;
                    height: 100vh; 
                    margin: 0;
                }
                h1, h2 {
                    text-align: center; 
                    color: ffffff; 
                }
            </style>
        </head>
        <body>
            <script type="text/javascript">
                function callJS() {
                    return 'Response from JS';
                }
                function callDesktop() {
                    window.cefQuery({
                            request: "1_callDesktop_{\"type\":\"1\"}",
                            onSuccess: function(response) {
                                // 处理Java应用程序的响应
                            },
                            onFailure: function(errorCode, errorMessage) {
                                // 处理错误
                            }
                        });
                }
                function callNative() {
                    window.JsBridge.callNative("Greet",JSON.stringify({type: "1"}));
                }
            </script>
            <h1>Compose WebView Multiplatform</h1>
            <h2 id="subtitle">Basic Html Test</h2>
            <button onclick="callNative()">callNative</button>
        </body>
        </html>
        """.trimIndent()
//    val webViewState = rememberWebViewStateWithHTMLFile(
//        fileName = "index.html",
//    )
    val webViewState = rememberWebViewStateWithHTMLData(html)
    LaunchedEffect(Unit) {
        webViewState.webSettings.apply {
            zoomLevel = 1.0
            isJavaScriptEnabled = true
            logSeverity = KLogSeverity.Debug
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            androidWebSettings.apply {
                isAlgorithmicDarkeningAllowed = true
                safeBrowsingEnabled = true
                allowFileAccess = true
            }
        }
    }
    webViewState.jsBridge.register(object : IJsHandler {
        override fun methodName(): String {
            return "Greet"
        }

        override fun handle(message: JsMessage, callback: (Any) -> Unit) {
            Logger.i {
                "Greet Handler Get Message: $message"
            }
            val param = processParams<GreetModel>(message)
            Logger.i {
                "Greet Handler Get Param: $param"
            }
            callback("KMM ${param.type}")
        }

    })
    val webViewNavigator = rememberWebViewNavigator()
    var jsRes by mutableStateOf("Evaluate JavaScript")
    MaterialTheme {
        Box(Modifier.fillMaxSize()) {
            WebView(
                state = webViewState,
                modifier = Modifier.fillMaxSize(),
                captureBackPresses = false,
                navigator = webViewNavigator,
            )
            Button(
                onClick = {
                    webViewNavigator.evaluateJavaScript(
                        """
                        document.getElementById("subtitle").innerText = "Hello from KMM!";
                        window.JsBridge.callNative("Greet",JSON.stringify({type: "1"}),
                            function (data) {
                                document.getElementById("subtitle").innerText = data;
                                console.log("Greet from Native: " + data);
                            }
                        );
                        callJS();
                        """.trimIndent(),
                    ) {
                        jsRes = it
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 50.dp),
            ) {
                Text(jsRes)
            }
        }
    }
}
