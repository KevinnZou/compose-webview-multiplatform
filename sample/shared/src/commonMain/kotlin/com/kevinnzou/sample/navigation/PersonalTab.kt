package com.kevinnzou.sample.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewState

/**
 * Created By Kevin Zou On 2023/12/8
 */
object PersonalTab : Tab {
    @Composable
    override fun Content() {
        Personal()
    }

    override val options: TabOptions
        @Composable
        get() {
            val title = "个人"
            val icon = rememberVectorPainter(Icons.Default.Person)

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
fun Personal() {
    val state = rememberWebViewState("https://www.bing.com/search?q=Android")
    WebView(state = state, modifier = Modifier.fillMaxSize())
}
