package com.multiplatform.webview.cookie

import platform.WebKit.WKHTTPCookieStore
import platform.WebKit.WKWebsiteDataStore

object IOSCookieManager: CookieManager {
    private val cookieStore: WKHTTPCookieStore = WKWebsiteDataStore.defaultDataStore().httpCookieStore

    override fun getCookies(url: String): List<Cookie> {
        TODO()
    }

    override fun removeAllCookies() {
        TODO()
    }

    override fun setCookie(url: String, cookie: Cookie) {
        TODO()
    }
}

actual fun getCookieExpirationDate(expiresDate: Long): String {
    TODO()
}

@Suppress("FunctionName") // Builder Function
actual fun ActualCookieManager(): CookieManager = IOSCookieManager