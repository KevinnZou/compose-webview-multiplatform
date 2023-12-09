package com.kevinnzou.sample.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import co.touchlab.kermit.Logger
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState

/**
 * Created By Kevin Zou On 2023/12/8
 */
object HomeTab : Tab {
    @Composable
    override fun Content() {
        val state =
            rememberWebViewState("https://github.com/KevinnZou/compose-webview-multiplatform")
        WebView(state = state, onDispose = {
            Logger.d { "WebView onDispose" }
        })
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
