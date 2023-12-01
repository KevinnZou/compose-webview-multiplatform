package com.kevinnzou.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            </script>
            <h1>Compose WebView Multiplatform</h1>
            <h2 id="subtitle">Basic Html Test</h2>
            <a href="https://docs.google.com/gview?embedded=true&url=http://www.expertagent.co.uk/asp/in4glestates/{16D968D6-198E-4E33-88F4-8A85731CE605}/{05c36123-4df0-4d7d-811c-8b6686fdd526}/external.pdf">Download PDF</a>
        </body>
        </html>
        """.trimIndent()
//    val webViewState = rememberWebViewStateWithHTMLFile(
//        fileName = "index.html",
//    )
    val webViewState = rememberWebViewStateWithHTMLData(html)
    webViewState.webSettings.apply {
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
