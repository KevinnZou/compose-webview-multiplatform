package com.multiplatform.webview.web

import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.request.WebRequestInterceptResult
import com.multiplatform.webview.util.KLogger
import dev.datlag.kcef.KCEFBrowser
import org.cef.CefSettings
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefDisplayHandler
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.network.CefRequest
import kotlin.math.abs
import kotlin.math.ln

/**
 * Created By Kevin Zou On 2023/9/12
 */
internal fun CefBrowser.getCurrentUrl(): String? {
    return this.url
}

internal fun CefBrowser.addDisplayHandler(state: WebViewState) {
    this.client.addDisplayHandler(
        object : CefDisplayHandler {
            override fun onAddressChange(
                browser: CefBrowser?,
                frame: CefFrame?,
                url: String?,
            ) {
                KLogger.d { "onAddressChange: $url" }
                state.lastLoadedUrl = getCurrentUrl()
            }

            override fun onTitleChange(
                browser: CefBrowser?,
                title: String?,
            ) {
                // https://magpcss.org/ceforum/viewtopic.php?t=11491
                // https://github.com/KevinnZou/compose-webview-multiplatform/issues/46
                val givenZoomLevel = state.webSettings.zoomLevel
                val realZoomLevel =
                    if (givenZoomLevel >= 0.0) {
                        ln(abs(givenZoomLevel)) / ln(1.2)
                    } else {
                        -ln(abs(givenZoomLevel)) / ln(1.2)
                    }
                KLogger.d { "titleProperty: $title" }
                zoomLevel = realZoomLevel
                state.pageTitle = title
            }

            override fun onFullscreenModeChange(
                p0: CefBrowser?,
                p1: Boolean,
            ) {
                // Not supported
            }

            override fun onTooltip(
                browser: CefBrowser?,
                text: String?,
            ): Boolean {
                return false
            }

            override fun onStatusMessage(
                browser: CefBrowser?,
                value: String?,
            ) {
            }

            override fun onConsoleMessage(
                browser: CefBrowser?,
                level: CefSettings.LogSeverity?,
                message: String?,
                source: String?,
                line: Int,
            ): Boolean {
                return false
            }

            override fun onCursorChange(
                browser: CefBrowser?,
                cursorType: Int,
            ): Boolean {
                return false
            }
        },
    )
}

internal fun CefBrowser.addLoadListener(
    state: WebViewState,
    navigator: WebViewNavigator,
) {
    this.client.addLoadHandler(
        object : CefLoadHandler {
            private var lastLoadedUrl = "null"

            override fun onLoadingStateChange(
                browser: CefBrowser?,
                isLoading: Boolean,
                canGoBack: Boolean,
                canGoForward: Boolean,
            ) {
                KLogger.d {
                    "onLoadingStateChange: $url, $isLoading $canGoBack $canGoForward"
                }
                if (isLoading) {
                    state.loadingState = LoadingState.Initializing
                } else {
                    state.loadingState = LoadingState.Finished
                    if (url != null && url != lastLoadedUrl) {
                        state.webView?.injectJsBridge()
                        lastLoadedUrl = url
                    }
                }
                navigator.canGoBack = canGoBack
                navigator.canGoForward = canGoForward
            }

            override fun onLoadStart(
                browser: CefBrowser?,
                frame: CefFrame?,
                transitionType: CefRequest.TransitionType?,
            ) {
                KLogger.d { "Load Start ${browser?.url}" }
                lastLoadedUrl = "null" // clean last loaded url for reload to work
                state.loadingState = LoadingState.Loading(0F)
                state.errorsForCurrentRequest.clear()
            }

            override fun onLoadEnd(
                browser: CefBrowser?,
                frame: CefFrame?,
                httpStatusCode: Int,
            ) {
                KLogger.d { "Load End ${browser?.url}" }
                state.loadingState = LoadingState.Finished
                navigator.canGoBack = canGoBack()
                navigator.canGoBack = canGoForward()
                state.lastLoadedUrl = getCurrentUrl()
            }

            override fun onLoadError(
                browser: CefBrowser?,
                frame: CefFrame?,
                errorCode: CefLoadHandler.ErrorCode?,
                errorText: String?,
                failedUrl: String?,
            ) {
                state.loadingState = LoadingState.Finished
                // TODO Error
                KLogger.i {
                    "Failed to load url: $errorCode ${failedUrl}\n$errorText"
                }
                state.errorsForCurrentRequest.add(
                    WebViewError(
                        code = errorCode?.code ?: 404,
                        description = "Failed to load url: ${failedUrl}\n$errorText",
                        isFromMainFrame = frame?.isMain ?: false,
                    ),
                )
            }
        },
    )
}

internal fun KCEFBrowser.addRequestHandler(
    state: WebViewState,
    navigator: WebViewNavigator,
) {
    client.addRequestHandler(
        object : CefRequestHandlerAdapter() {
            override fun onBeforeBrowse(
                browser: CefBrowser?,
                frame: CefFrame?,
                request: CefRequest?,
                userGesture: Boolean,
                isRedirect: Boolean,
            ): Boolean {
                navigator.requestInterceptor?.apply {
                    val map = mutableMapOf<String, String>()
                    request?.getHeaderMap(map)
                    KLogger.d { "onBeforeBrowse ${request?.url} $map" }
                    val webRequest =
                        WebRequest(
                            request?.url.toString(),
                            map,
                            isForMainFrame = frame?.isMain ?: false,
                            isRedirect = isRedirect,
                            request?.method ?: "GET",
                        )
                    val interceptResult =
                        this.onInterceptUrlRequest(
                            webRequest,
                            navigator,
                        )
                    return when (interceptResult) {
                        is WebRequestInterceptResult.Allow -> {
                            super.onBeforeBrowse(browser, frame, request, userGesture, isRedirect)
                        }

                        is WebRequestInterceptResult.Reject -> {
                            true
                        }

                        is WebRequestInterceptResult.Modify -> {
                            interceptResult.request.apply {
                                navigator.loadUrl(this.url, this.headers)
                            }
                            true
                        }
                    }
                }
                return super.onBeforeBrowse(browser, frame, request, userGesture, isRedirect)
            }
        },
    )
}
