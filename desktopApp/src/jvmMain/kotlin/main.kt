import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.multiplatform.webview.MainWebView
import com.multiplatform.webview.web.Cef
import java.io.File

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