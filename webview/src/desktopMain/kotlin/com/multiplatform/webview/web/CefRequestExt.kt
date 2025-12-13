package com.multiplatform.webview.web

import com.multiplatform.webview.setting.WebSettings
import dev.datlag.kcef.KCEFResourceRequestHandler
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefRequestHandler
import org.cef.handler.CefRequestHandlerAdapter
import org.cef.handler.CefResourceRequestHandler
import org.cef.misc.BoolRef
import org.cef.network.CefRequest

internal fun createModifiedRequestHandler(settings: WebSettings): CefRequestHandler {
    return object : CefRequestHandlerAdapter() {
        override fun getResourceRequestHandler(
            browser: CefBrowser?,
            frame: CefFrame?,
            request: CefRequest?,
            isNavigation: Boolean,
            isDownload: Boolean,
            requestInitiator: String?,
            disableDefaultHandling: BoolRef?,
        ): CefResourceRequestHandler {
            return object : KCEFResourceRequestHandler(
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
}

internal fun CefRequest.setUserAgentString(userAgent: String) {
    setHeaderByName("User-Agent", userAgent, true)
}
