package com.kevinnzou.sample

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import co.touchlab.kermit.Logger
import com.kevinnzou.sample.eventbus.FlowEventBus
import com.kevinnzou.sample.eventbus.NavigationEvent
import com.kevinnzou.sample.jsbridge.GreetJsMessageHandler
import com.kevinnzou.sample.res.HtmlRes
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import com.multiplatform.webview.jsbridge.rememberWebViewJsBridge
import com.multiplatform.webview.util.KLogSeverity
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewState
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewSnapshot
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLFile
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * Created By Kevin Zou On 2023/9/8
 *
 * Basic Sample for loading Html in WebView
 *
 * Note: Developers targeting the Desktop platform should refer to
 * [README.desktop.md](https://github.com/KevinnZou/compose-webview-multiplatform/blob/main/README.desktop.md)
 * for setup instructions first.
 */
@Composable
internal fun BasicWebViewWithHTMLSample(navHostController: NavHostController? = null) {
    val html = HtmlRes.html
    val webViewState =
        rememberWebViewStateWithHTMLFile(
            fileName = "index.html",
        )
//    val webViewState = rememberWebViewStateWithHTMLData(html)
    val webViewNavigator = rememberWebViewNavigator()
    val jsBridge = rememberWebViewJsBridge(webViewNavigator)
    var jsRes by mutableStateOf("Evaluate JavaScript")
    val webViewSnapshot = rememberWebViewSnapshot()

    val coroutineScope = rememberCoroutineScope()
    var webViewSnapshotResult: ImageBitmap? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        initWebView(webViewState)
        initJsBridge(jsBridge)
    }
    MaterialTheme {
        Column {
            TopAppBar(
                title = { Text(text = "Html Sample") },
                navigationIcon = {
                    IconButton(onClick = {
                        navHostController?.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )

            Box(Modifier.fillMaxSize()) {
                WebView(
                    state = webViewState,
                    modifier = Modifier.fillMaxSize(),
                    captureBackPresses = false,
                    navigator = webViewNavigator,
                    webViewJsBridge = jsBridge,
                    webViewSnapshot = webViewSnapshot
                )
                Column(
                    Modifier.align(Alignment.BottomCenter).padding(bottom = 30.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            webViewNavigator.evaluateJavaScript(
                                """
                            document.getElementById("subtitle").innerText = "Hello from KMM!";
                            window.kmpJsBridge.callNative("Greet",JSON.stringify({message: "Hello"}),
                                function (data) {
                                    document.getElementById("subtitle").innerText = data;
                                    console.log("Greet from Native: " + data);
                                }
                            );
                            callJS();
                            """.trimIndent(),
                            ) {
                                jsRes = it
                            }
                        },
                        modifier = Modifier,
                    ) {
                        Text(jsRes)
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                webViewSnapshotResult = webViewSnapshot.takeSnapshot(null)
                            }
                        },
                        modifier = Modifier,
                    ) {
                        Text("Take Snapshot")
                    }
                }

                // When WebView's Bitmap image is captured, show snapshot in dialog
                webViewSnapshotResult?.let { bitmap ->
                    Dialog(onDismissRequest = { }) {
                        Column(
                            modifier = Modifier
                                .background(LightGray)
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Preview of WebView snapshot")
                            Spacer(Modifier.size(16.dp))
                            Image(
                                bitmap = bitmap,
                                contentDescription = "Preview of WebViewSnapshot"
                            )
                            Spacer(Modifier.size(4.dp))
                            Button(onClick = { webViewSnapshotResult = null }) {
                                Text("Close Snapshot Preview")
                            }
                        }
                    }
                }
            }
        }
    }
}

fun initWebView(webViewState: WebViewState) {
    webViewState.webSettings.apply {
        zoomLevel = 1.0
        isJavaScriptEnabled = true
        logSeverity = KLogSeverity.Debug
        allowFileAccessFromFileURLs = true
        allowUniversalAccessFromFileURLs = true
        androidWebSettings.apply {
            isAlgorithmicDarkeningAllowed = true
            safeBrowsingEnabled = true
            allowFileAccess = true
        }
    }
}

suspend fun initJsBridge(webViewJsBridge: WebViewJsBridge) {
    webViewJsBridge.register(GreetJsMessageHandler())
    //        EventBus.observe<NavigationEvent> {
//            Logger.d {
//                "Received NavigationEvent"
//            }
//        }
    FlowEventBus.events.filter { it is NavigationEvent }.collect {
        Logger.d {
            "Received NavigationEvent"
        }
    }
}
