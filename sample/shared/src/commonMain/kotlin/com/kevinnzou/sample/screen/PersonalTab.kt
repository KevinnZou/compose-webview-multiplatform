package com.kevinnzou.sample.screen

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
//        var count by rememberSaveable { mutableStateOf(0) }
//        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            Column {
//                Text(text = "Count $count", fontSize = 30.sp)
//                Button(onClick = { count++ }) {
//                    Text(text = "Count Up")
//                }
//            }
//        }
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
    val state = rememberWebViewState("https://kotlinlang.org/docs/multiplatform.html")
    WebView(state = state, modifier = Modifier.fillMaxSize())
}
