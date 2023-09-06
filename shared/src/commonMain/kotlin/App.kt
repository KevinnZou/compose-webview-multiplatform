import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import web.WebView
import web.rememberWebViewState

@Composable
internal fun App() {
    WebViewSample()
}

@Composable
internal fun WebViewSample() {
    MaterialTheme {
        val webViewState = rememberWebViewState("https://developer.android.com/")
        Column(Modifier.fillMaxSize()) {
            val text = webViewState.let {
                "${it.pageTitle ?: ""} ${it.loadingState} ${it.lastLoadedUrl ?: ""}"
            }
            Text(text)
            WebView(
                state = webViewState,
                modifier = Modifier.weight(1f)
            )
        }

    }
}

expect fun getPlatformName(): String