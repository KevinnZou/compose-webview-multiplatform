package com.multiplatform.webview.cookie

object DesktopCookieManager: CookieManager {
    override fun setCookie(url: String, cookie: Cookie) {
        TODO("Not yet implemented")
    }

    override fun getCookies(url: String): List<Cookie> {
        TODO("Not yet implemented")
    }

    override fun removeAllCookies() {
        TODO("Not yet implemented")
    }
}

actual fun getCookieExpirationDate(expiresDate: Long): String {
    TODO()
}

@Suppress("FunctionName") // Builder Function
actual fun ActualCookieManager(): CookieManager = DesktopCookieManager