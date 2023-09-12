package sample

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
import web.LoadingState
import web.WebView
import web.rememberWebViewNavigator
import web.rememberWebViewState

/**
 * Created By Kevin Zou On 2023/9/8
 */
@Composable
internal fun BasicWebViewSample() {
    val initialUrl = "https://developer.android.com/"
    val state = rememberWebViewState(url = initialUrl)
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
                                contentDescription = "Back"
                            )
                        }
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
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = textFieldValue ?: "",
                        onValueChange = { textFieldValue = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {
                        textFieldValue?.let {
                            navigator.loadUrl(it)
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Text("Go")
                }
            }

            val loadingState = state.loadingState
            if (loadingState is LoadingState.Loading) {
                LinearProgressIndicator(
                    progress = loadingState.progress,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            WebView(
                state = state,
                modifier = Modifier
                    .fillMaxSize(),
                navigator = navigator,
            )
        }
    }

}