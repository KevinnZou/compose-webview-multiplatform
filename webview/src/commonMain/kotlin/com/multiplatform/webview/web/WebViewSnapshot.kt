package com.multiplatform.webview.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Created By Murod 2024/07/20
 */

/**
 * Allows capturing a snapshot of the webview.
 *
 * @see [rememberWebViewSnapshot]
 */
class WebViewSnapshot {
    internal var webView: IWebView? = null

    /**
     * Captures a snapshot of the `WebView` within the specified rectangle.
     *
     * @param rect An optional `Rect` specifying the area to capture. If `null`, captures the entire `WebView`.
     * @return An `ImageBitmap` containing the snapshot, or `null` if the capture fails.
     */
    suspend fun takeSnapshot(rect: Rect? = null): ImageBitmap? {
        return suspendCancellableCoroutine {
            webView?.takeSnapshot(
                rect = rect,
                completionHandler = { img ->
                    it.resume(img)
                }
            )
        }
    }
}

/**
 * Creates and remembers a [WebViewSnapshot]
 *
 * @return An instance of `WebViewSnapshot`.
 */
@Composable
fun rememberWebViewSnapshot(): WebViewSnapshot {
    return remember { WebViewSnapshot() }
}