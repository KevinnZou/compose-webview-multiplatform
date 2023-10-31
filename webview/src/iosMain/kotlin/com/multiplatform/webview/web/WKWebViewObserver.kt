package com.multiplatform.webview.web

import com.multiplatform.webview.util.KLogger
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import observer.ObserverProtocol
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.darwin.NSObject

/**
 * Created By Kevin Zou On 2023/9/13
 */

/**
 * Observer for the WKWebView's loading state
 */
@ExperimentalForeignApi
class WKWebViewObserver(private val state: WebViewState, private val navigator: WebViewNavigator) :
    NSObject(),
    ObserverProtocol {
    override fun observeValueForKeyPath(
        keyPath: String?,
        ofObject: Any?,
        change: Map<Any?, *>?,
        context: COpaquePointer?,
    ) {
        if (keyPath == "estimatedProgress") {
            val progress = change?.get("new") as? NSNumber
            KLogger.d { "Observe estimatedProgress Changed $progress" }
            if (progress != null) {
                state.loadingState = LoadingState.Loading(progress.floatValue)
                if (progress.floatValue >= 1.0f) {
                    state.loadingState = LoadingState.Finished
                }
            }
        } else if (keyPath == "title") {
            val title = change?.get("new") as? String
            KLogger.d { "Observe title Changed $title" }
            if (title != null) {
                state.pageTitle = title
            }
        } else if (keyPath == "URL") {
            val url = change?.get("new") as? NSURL
            KLogger.d { "Observe URL Changed ${url?.absoluteString}" }
            if (url != null) {
                state.lastLoadedUrl = url.absoluteString
            }
        } else if (keyPath == "canGoBack") {
            val canGoBack = change?.get("new") as? NSNumber
            KLogger.d { "Observe canGoBack Changed $canGoBack" }
            if (canGoBack != null) {
                navigator.canGoBack = canGoBack.boolValue
            }
        } else if (keyPath == "canGoForward") {
            val canGoForward = change?.get("new") as? NSNumber
            KLogger.d { "Observe canGoForward Changed $canGoForward" }
            if (canGoForward != null) {
                navigator.canGoForward = canGoForward.boolValue
            }
        }
    }
}
