package com.kevinnzou.sample

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.multiplatform.webview.cookie.Cookie
import com.multiplatform.webview.util.KLogSeverity
import com.multiplatform.webview.web.LoadingState
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState

/**
 * Created By Kevin Zou On 2023/9/8
 */
@Composable
internal fun BasicWebViewSample() {
    val initialUrl =
        "https://docs.google.com/gview?embedded=true&url=http://www.expertagent.co.uk/asp/in4glestates/{16D968D6-198E-4E33-88F4-8A85731CE605}/{05c36123-4df0-4d7d-811c-8b6686fdd526}/external.pdf"
    val state = rememberWebViewState(url = initialUrl)
    state.webSettings.apply {
        logSeverity = KLogSeverity.Debug
        customUserAgentString =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_1) AppleWebKit/625.20 (KHTML, like Gecko) Version/14.3.43 Safari/625.20"
    }
    val navigator = rememberWebViewNavigator()
    var textFieldValue by remember(state.lastLoadedUrl) {
        mutableStateOf(state.lastLoadedUrl)
    }
    MaterialTheme {
        Column {
            TopAppBar(
                title = { Text(text = "WebView Sample") },
                navigationIcon = {
                    if (navigator.canGoBack) {
                        IconButton(onClick = { navigator.navigateBack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }
                },
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

            LaunchedEffect(state.loadingState) {
                if (state.loadingState is LoadingState.Finished) {
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

            WebView(
                state = state,
                modifier =
                    Modifier
                        .fillMaxSize(),
                navigator = navigator,
            )
        }
    }
}
