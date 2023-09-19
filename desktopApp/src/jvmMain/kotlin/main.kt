import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.multiplatform.webview.MainWebView

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        MainWebView()
    }
}