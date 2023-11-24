package com.multiplatform.webview.web

import org.cef.browser.CefBrowser
import org.cef.browser.CefFrame
import org.cef.handler.CefLifeSpanHandlerAdapter

class DisablePopupWindowsLifeSpanHandler : CefLifeSpanHandlerAdapter() {
    override fun onBeforePopup(
        browser: CefBrowser?,
        frame: CefFrame?,
        target_url: String?,
        target_frame_name: String?,
    ): Boolean {
        if (target_url != null) {
            browser?.loadURL(target_url)
        }
        return true
    }
}
