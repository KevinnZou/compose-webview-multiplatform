package com.kevinnzou.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.multiplatform.webview.util.KLogSeverity
import com.multiplatform.webview.web.WebView
import com.multiplatform.webview.web.rememberWebViewNavigator
import com.multiplatform.webview.web.rememberWebViewState

/**
 * Created By Adam Kobor On 2024/9/9
 *
 * Sample for MIDI SysEx handling in WebView.
 * `allowMidiSysexMessages` is set to true in the WebView settings, so MIDI SysEx messages can be
 * sent and received.
 */
@Composable
internal fun MidiSample(navHostController: NavHostController? = null) {
    val url = "https://versioduo.com/webmidi-test/"
    val state = rememberWebViewState(url = url)
    DisposableEffect(Unit) {
        state.webSettings.apply {
            logSeverity = KLogSeverity.Debug
            customUserAgentString =
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_1) AppleWebKit/625.20 (KHTML, like Gecko) Version/14.3.43 Safari/625.20"
            androidWebSettings.allowMidiSysexMessages = true
        }

        onDispose { }
    }
    val navigator = rememberWebViewNavigator()

    MaterialTheme {
        Column {
            TopAppBar(
                title = { Text(text = "Midi SysEx Sample") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (navigator.canGoBack) {
                            navigator.navigateBack()
                        } else {
                            navHostController?.popBackStack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )

            WebView(
                state = state,
                modifier = Modifier.fillMaxSize(),
                navigator = navigator,
            )
        }
    }
}
