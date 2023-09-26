package com.multiplatform.webview.web

import com.multiplatform.webview.setting.WebSettings
import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.browser.CefRequestContext
import org.cef.handler.CefRequestContextHandlerAdapter
import org.cef.handler.CefResourceRequestHandler
import org.cef.handler.CefResourceRequestHandlerAdapter
import org.cef.misc.BoolRef
import org.cef.network.CefRequest

internal fun createModifiedRequestContext(
    settings: WebSettings
): CefRequestContext {
    val resourceRequestHandler = object : CefResourceRequestHandlerAdapter() {
        override fun onBeforeResourceLoad(
            browser: CefBrowser?,
            frame: CefFrame?,
            request: CefRequest?
        ): Boolean {
            if (request != null) {
                settings.customUserAgentString?.let(request::setUserAgentString)
            }
            return false
        }
    }

    return CefRequestContext.createContext(
        object : CefRequestContextHandlerAdapter() {
            override fun getResourceRequestHandler(
                browser: CefBrowser?,
                frame: CefFrame?,
                request: CefRequest?,
                isNavigation: Boolean,
                isDownload: Boolean,
                requestInitiator: String?,
                disableDefaultHandling: BoolRef?
            ): CefResourceRequestHandler {
                return resourceRequestHandler
            }
        })
}

internal fun CefRequest.setUserAgentString(userAgent: String) {
    setHeaderByName("User-Agent", userAgent, true)
}