package com.multiplatform.webview.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLData

/**
 * Created By Kevin Zou On 2023/9/8
 */
@Composable
internal fun BasicWebViewWithHTMLSample() {
    val html = """
        <html>
        <head>
            <title>Compose WebView Multiplatform</title>
            <style>
                body {
                    background-color: #e0e8f0; 
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    height: 100vh; 
                    margin: 0;
                }
                h1 {
                    text-align: center; 
                    color: white; 
                }
            </style>
        </head>
        <body>
            <h1>Compose WebView Multiplatform</h1>
        </body>
        </html>
    """.trimIndent()
    val webViewState = rememberWebViewStateWithHTMLData(
        data = html
    )
    MaterialTheme {
        Box(Modifier.fillMaxSize()) {
            WebView(
                state = webViewState,
                modifier = Modifier.matchParentSize()
                    .heightIn(min = 1.dp), // A bottom sheet can't support content with 0 height.
                captureBackPresses = false,
            )
        }
    }

}