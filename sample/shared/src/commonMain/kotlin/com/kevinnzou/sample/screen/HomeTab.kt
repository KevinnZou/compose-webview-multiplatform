package com.kevinnzou.sample.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import co.touchlab.kermit.Logger
import com.multiplatform.webview.util.KLogSeverity
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberSaveableWebViewState
import com.multiplatform.webview.web.rememberWebViewNavigator

/**
 * Created By Kevin Zou On 2023/12/8
 */
object HomeTab : Tab {
    @Composable
    override fun Content() {
        Home()
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = "主页"
            val icon = rememberVectorPainter(Icons.Default.Home)

            return remember {
                TabOptions(
                    index = 0u,
                    title = title,
                    icon = icon,
                )
            }
        }
}

@Composable
fun Home() {
    val webViewState =
        rememberSaveableWebViewState().apply {
            webSettings.logSeverity = KLogSeverity.Debug
        }

    val navigator = rememberWebViewNavigator()

    LaunchedEffect(navigator) {
        val bundle = webViewState.viewState
        if (bundle == null) {
            // This is the first time load, so load the home page.
            // Test Url: https://en.wikipedia.org/wiki/Foo_Fighters
            navigator.loadUrl("https://kotlinlang.org/docs/multiplatform.html")
        }
    }

    WebView(
        state = webViewState,
        modifier = Modifier.fillMaxSize(),
        navigator = navigator,
        onDispose = {
            Logger.d(tag = "ComposeWebView") {
                "WebView onDispose"
            }
        },
    )
}
