package com.multiplatform.webview.web

import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.util.KLogger
import org.cef.CefSettings
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.callback.CefAuthCallback
import org.cef.callback.CefCallback
import org.cef.handler.CefDisplayHandler
import org.cef.handler.CefLoadHandler
import org.cef.handler.CefRequestHandler
import org.cef.handler.CefResourceRequestHandler
import org.cef.misc.BoolRef
import org.cef.network.CefRequest
import org.cef.security.CefSSLInfo

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
                state.lastLoadedUrl = getCurrentUrl()
            }

            override fun onTitleChange(
                browser: CefBrowser?,
                title: String?,
            ) {
                KLogger.d { "titleProperty: $title" }
                state.pageTitle = title
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
            ) {}

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
            override fun onLoadingStateChange(
                browser: CefBrowser?,
                isLoading: Boolean,
                canGoBack: Boolean,
                canGoForward: Boolean,
            ) {
                if (isLoading) {
                    state.loadingState = LoadingState.Initializing
                } else {
                    state.loadingState = LoadingState.Finished
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
                state.loadingState = LoadingState.Loading(0F)
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
                KLogger.i {
                    "Failed to load url: ${failedUrl}\n$errorText"
                }
                state.errorsForCurrentRequest.add(
                    WebViewError(
                        code = errorCode?.code ?: 404,
                        description = "Failed to load url: ${failedUrl}\n$errorText",
                    ),
                )
            }
        },
    )
}

internal fun CefBrowser.addRequestHandler(
    state: WebViewState,
    navigator: WebViewNavigator,
) {
    this.client.addRequestHandler(
        object : CefRequestHandler {
            override fun onBeforeBrowse(
                browser: CefBrowser?,
                frame: CefFrame?,
                request: CefRequest?,
                userGesture: Boolean,
                isRedirect: Boolean,
            ): Boolean {
                val map = mutableMapOf<String, String>()
                request?.getHeaderMap(map)
                KLogger.d { "onBeforeBrowse ${request?.url} $map" }
                val webRequest =
                    WebRequest(
                        request?.url.toString(),
                        map,
                    )
                val intercept =
                    navigator.requestInterceptor?.beforeRequest(
                        webRequest,
                        navigator,
                    )
                return intercept ?: false
            }

            override fun onOpenURLFromTab(
                p0: CefBrowser?,
                p1: CefFrame?,
                p2: String?,
                p3: Boolean,
            ): Boolean {
                return false
            }

            override fun getResourceRequestHandler(
                p0: CefBrowser?,
                p1: CefFrame?,
                p2: CefRequest?,
                p3: Boolean,
                p4: Boolean,
                p5: String?,
                p6: BoolRef?,
            ): CefResourceRequestHandler? {
                return null
            }

            override fun getAuthCredentials(
                p0: CefBrowser?,
                p1: String?,
                p2: Boolean,
                p3: String?,
                p4: Int,
                p5: String?,
                p6: String?,
                p7: CefAuthCallback?,
            ): Boolean {
                return false
            }

            override fun onCertificateError(
                p0: CefBrowser?,
                p1: CefLoadHandler.ErrorCode?,
                p2: String?,
                p3: CefSSLInfo?,
                p4: CefCallback?,
            ): Boolean {
                return false
            }

            override fun onRenderProcessTerminated(
                p0: CefBrowser?,
                p1: CefRequestHandler.TerminationStatus?,
            ) {
            }
        },
    )
}
