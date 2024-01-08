package com.multiplatform.webview.web

import com.multiplatform.webview.setting.WebSettings
import dev.datlag.kcef.KCEFResourceRequestHandler
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefRequestContext
import org.cef.network.CefRequest

internal fun createModifiedRequestContext(settings: WebSettings): CefRequestContext {
    return CefRequestContext.createContext { browser, frame, request, isNavigation, isDownload, requestInitiator, disableDefaultHandling ->
        object : KCEFResourceRequestHandler(
            getGlobalDefaultHandler(browser, frame, request, isNavigation, isDownload, requestInitiator, disableDefaultHandling),
        ) {
            override fun onBeforeResourceLoad(
                browser: CefBrowser?,
                frame: CefFrame?,
                request: CefRequest?,
            ): Boolean {
                if (request != null) {
                    settings.customUserAgentString?.let(request::setUserAgentString)
                }
                return super.onBeforeResourceLoad(browser, frame, request)
            }
        }
    }
}

internal fun CefRequest.setUserAgentString(userAgent: String) {
    setHeaderByName("User-Agent", userAgent, true)
}
