package com.kevinnzou.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.multiplatform.webview.util.KLogSeverity
import com.multiplatform.webview.web.PlatformWebViewParams
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewStateWithHTMLFile

/**
 * Created By briandr97 2024/8/8
 *
 * Basic Sample for choose file in webview
 */
@Composable
internal fun FileChooseWebViewSample(navHostController: NavHostController? = null) {
    val webViewState = rememberWebViewStateWithHTMLFile(fileName = "fileChoose.html")
    val webViewNavigator = rememberWebViewNavigator()
    LaunchedEffect(Unit) {
        webViewState.webSettings.apply {
            zoomLevel = 1.0
            logSeverity = KLogSeverity.Debug
            androidWebSettings.apply {
                isAlgorithmicDarkeningAllowed = true
                safeBrowsingEnabled = true
                allowFileAccess = false
            }
        }
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
                    platformWebViewParams = getPlatformWebViewParams()
                )
            }
        }
    }
}

@Composable
expect fun getPlatformWebViewParams(): PlatformWebViewParams?
