package web

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun ActualWebView(
    state: WebViewState,
    modifier: Modifier,
    captureBackPresses: Boolean,
    navigator: WebViewNavigator,
    onCreated: () -> Unit,
    onDispose: () -> Unit,
) {
    AccompanistWebView(
        state,
        modifier,
        captureBackPresses,
        navigator,
        onCreated = { _ -> onCreated() },
        onDispose = { _ -> onDispose() })
}