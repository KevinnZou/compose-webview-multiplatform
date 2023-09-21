import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.multiplatform.webview.MainWebView
import com.multiplatform.webview.web.Cef
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.time.Duration.Companion.seconds

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        var restartRequired by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            Cef.init(builder = {
                installDir = File("jcef-bundle")
            }, onError = {
                it.printStackTrace()
            }) {
                restartRequired = true
            }
        }

        if (restartRequired) {
            Text(text = "Restart required.")
        } else {
            MainWebView()
        }
    }
}