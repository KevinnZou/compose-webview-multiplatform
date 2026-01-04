package com.kevinnzou.sample

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import co.touchlab.kermit.Logger
import com.multiplatform.webview.jsbridge.rememberWebViewJsBridge
import com.multiplatform.webview.response.ErrorResponse
import com.multiplatform.webview.response.ErrorResponseInterceptor
import com.multiplatform.webview.response.ShouldStopLoading
import com.multiplatform.webview.util.KLogSeverity
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.WebViewFileReadType
import com.multiplatform.webview.web.WebViewNavigator
import com.multiplatform.webview.web.WebViewState
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLFile
import compose_webview_multiplatform.sample.shared.generated.resources.Res

/**
 * Created By Matthias Hennemeyer On 2025/8/8
 *
 * Sample intercepting an error response
 *
 * Note: Developers targeting the Desktop platform should refer to
 * [README.desktop.md](https://github.com/KevinnZou/compose-webview-multiplatform/blob/main/README.desktop.md)
 * for setup instructions first.
 */
@Composable
internal fun ErrorResponseSample(navHostController: NavHostController? = null) {
    val webViewState =
        rememberWebViewStateWithHTMLFile(
            fileName = Res.getUri("files/samples/errorResponse.html"),
            readType = WebViewFileReadType.COMPOSE_RESOURCE_FILES,
        )
    val errorUrl = "http://matthiashennemeyer.com/404-on-android-and-tls-error-on-ios"
    var errorResponse by remember { mutableStateOf<ErrorResponse?>(null) }
    val webViewNavigator = rememberWebViewNavigator(
        errorResponseInterceptor = object : ErrorResponseInterceptor {
            override fun onInterceptErrorResponse(
                response: ErrorResponse,
                navigator: WebViewNavigator
            ): ShouldStopLoading {
                Logger.e("errorResponseInterceptor: code: ${response.errorCode} description: ${response.description}")
                errorResponse = response
                return true
            }
        }
    )
    val jsBridge = rememberWebViewJsBridge(webViewNavigator)
    LaunchedEffect(Unit) {
        init(webViewState)
    }
    MaterialTheme {
        Scaffold { innerPadding ->
            Column(
                modifier =
                    Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
            ) {
                Column {
                    TopAppBar(
                        modifier =
                            Modifier
                                .background(
                                    color = MaterialTheme.colors.primary,
                                ).padding(
                                    top =
                                        WindowInsets.statusBars
                                            .asPaddingValues()
                                            .calculateTopPadding(),
                                ),
                        title = { Text(text = "Error Response Sample") },
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
                        actions = {
                            Button(
                                onClick = {
                                    webViewNavigator.loadUrl(errorUrl)
                                },
                                modifier = Modifier.padding(horizontal = 4.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        backgroundColor = Color.White,
                                        contentColor = MaterialTheme.colors.primary,
                                    ),
                            ) {
                                Text(
                                    "Trigger Error",
                                    style = MaterialTheme.typography.caption,
                                )
                            }
                        },

                    )

                    AnimatedVisibility(visible = (errorResponse != null)) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(horizontal = 4.dp)
                                ,
                                onClick = { errorResponse = null }
                            ) { Text(text = "X") }
                        }


                        if (errorResponse != null) {
                            Text(text = "Error: ${errorResponse!!.description}\nCode: ${errorResponse!!.errorCode}")
                        }
                    }

                    // WebView without overlay buttons
                    WebView(
                        state = webViewState,
                        modifier = Modifier.fillMaxSize(),
                        captureBackPresses = false,
                        navigator = webViewNavigator,
                        webViewJsBridge = jsBridge,
                    )
                }
            }
        }
    }
}

fun init(webViewState: WebViewState) {
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
