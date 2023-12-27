package com.multiplatform.webview.web

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.addObserver
import platform.Foundation.removeObserver
import platform.WebKit.WKWebView
import platform.darwin.NSObject

/**
 * Created By Kevin Zou On 2023/9/13
 */
val observedProgressList =
    listOf(
        "estimatedProgress",
        "title",
        "URL",
        "canGoBack",
        "canGoForward",
    )

/**
 * Adds observers for the given properties
 */
@OptIn(ExperimentalForeignApi::class)
fun WKWebView.addObservers(
    observer: NSObject,
    properties: List<String>,
) {
    properties.forEach {
        this.addObserver(
            observer,
            forKeyPath = it,
            options = platform.Foundation.NSKeyValueObservingOptionNew,
            context = null,
        )
    }
}

/**
 * Removes observers for the given properties
 */
fun WKWebView.removeObservers(
    observer: NSObject,
    properties: List<String>,
) {
    properties.forEach {
        this.removeObserver(observer, forKeyPath = it)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun WKWebView.addProgressObservers(observer: NSObject) {
    this.addObservers(
        observer = observer,
        properties = observedProgressList,
    )
}

/**
 * Removes observers for the given properties
 */
fun WKWebView.removeProgressObservers(observer: NSObject) {
    this.removeObservers(
        observer = observer,
        properties = observedProgressList,
    )
}
