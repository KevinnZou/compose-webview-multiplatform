package com.kevinnzou.sample

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import co.touchlab.kermit.Logger
import com.multiplatform.webview.cookie.Cookie
import com.multiplatform.webview.util.KLogSeverity
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewState
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewSnapshot
import com.multiplatform.webview.web.rememberWebViewState
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * Created By Kevin Zou On 2023/9/8
 *
 * Basic Sample of WebView
 *
 * Note: Developers targeting the Desktop platform should refer to
 * [README.desktop.md](https://github.com/KevinnZou/compose-webview-multiplatform/blob/main/README.desktop.md)
 * for setup instructions first.
 */
@Composable
internal fun BasicWebViewSample(navHostController: NavHostController? = null) {
    val initialUrl = "https://github.com/KevinnZou/compose-webview-multiplatform"
    val state =
        rememberWebViewState(url = initialUrl) {
            logSeverity = KLogSeverity.Debug
            customUserAgentString =
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_1) AppleWebKit/625.20 (KHTML, like Gecko) Version/14.3.43 Safari/625.20"
            iOSWebSettings.isInspectable = true
        }
    val navigator = rememberWebViewNavigator()
    val webViewSnapshot = rememberWebViewSnapshot()
    val coroutineScope = rememberCoroutineScope()
    var webViewSnapshotResult: ImageBitmap? by remember { mutableStateOf(null) }

    var textFieldValue by remember(state.lastLoadedUrl) {
        mutableStateOf(state.lastLoadedUrl)
    }
    MaterialTheme {
        Column {
            TopAppBar(
                title = { Text(text = "WebView Sample") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (navigator.canGoBack) {
                            navigator.navigateBack()
                        } else {
                            navHostController?.popBackStack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            webViewSnapshotResult = webViewSnapshot.takeSnapshot(
                                Rect(
                                    top = 50f,
                                    left = 50f,
                                    right = 1000f,
                                    bottom = 2000f
                                )
                            )
                        }
                    }) {
                        Text("Snapshot", color = Color.White)
                    }
                }
            )

            Row {
                Box(modifier = Modifier.weight(1f)) {
                    if (state.errorsForCurrentRequest.isNotEmpty()) {
                        Image(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Error",
                            colorFilter = ColorFilter.tint(Color.Red),
                            modifier =
                            Modifier
                                .align(Alignment.CenterEnd)
                                .padding(8.dp),
                        )
                    }

                    OutlinedTextField(
                        value = textFieldValue ?: "",
                        onValueChange = { textFieldValue = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Button(
                    onClick = {
                        textFieldValue?.let {
                            navigator.loadUrl(it)
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically),
                ) {
                    Text("Go")
                }
            }

            val loadingState = state.loadingState
            if (loadingState is LoadingState.Loading) {
                LinearProgressIndicator(
                    progress = loadingState.progress,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Box {
                WebView(
                    state = state,
                    modifier =
                    Modifier
                        .fillMaxSize(),
                    navigator = navigator,
                    webViewSnapshot = webViewSnapshot
                )

                // When WebView's Bitmap image is captured, show snapshot in dialog
                webViewSnapshotResult?.let { bitmap ->
                    Dialog(onDismissRequest = { }) {
                        Box {
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
                            }

                            Button(
                                onClick = { webViewSnapshotResult = null },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            ) {
                                Text("Close Snapshot Preview")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun CookieSample(state: WebViewState) {
    LaunchedEffect(state) {
        snapshotFlow { state.loadingState }
            .filter { it is LoadingState.Finished }
            .collect {
                state.cookieManager.setCookie(
                    "https://github.com",
                    Cookie(
                        name = "test",
                        value = "value",
                        domain = "github.com",
                        expiresDate = 1896863778,
                    ),
                )
                Logger.i {
                    "cookie: ${state.cookieManager.getCookies("https://github.com")}"
                }
                state.cookieManager.removeAllCookies()
                Logger.i {
                    "cookie: ${state.cookieManager.getCookies("https://github.com")}"
                }
            }
    }
}
