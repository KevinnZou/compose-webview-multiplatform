package com.multiplatform.webview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.multiplatform.webview.sample.BasicWebViewSample
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState

@Composable
internal fun WebViewApp() {
//    WebViewSample()
    BasicWebViewSample()
//    BasicWebViewWithHTMLSample()
}

@Composable
internal fun WebViewSample() {
    MaterialTheme {
        val webViewState = rememberWebViewState("https://github.com/KevinnZou/compose-webview-multiplatform")
        Column(Modifier.fillMaxSize()) {
            val text = webViewState.let {
                "${it.pageTitle ?: ""} ${it.loadingState} ${it.lastLoadedUrl ?: ""}"
            }
            Text(text)
            WebView(
                state = webViewState,
                modifier = Modifier.fillMaxSize()
            )
        }

    }
}

expect fun getPlatformName(): String