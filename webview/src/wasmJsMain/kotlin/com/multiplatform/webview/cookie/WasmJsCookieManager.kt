package com.multiplatform.webview.cookie

/**
 * WasmJs implementation of [CookieManager]
 */
class WasmJsCookieManager : CookieManager {
    override suspend fun setCookie(
        url: String,
        cookie: Cookie,
    ) {
        // TODO
    }

    override suspend fun getCookies(url: String): List<Cookie> {
        return emptyList()
    }

    override suspend fun removeAllCookies() {
        // TODO
    }

    override suspend fun removeCookies(url: String) {
        // TODO
    }
}

/**
 * Creates a [CookieManager] instance.
 */
@Suppress("FunctionName")
actual fun WebViewCookieManager(): CookieManager {
    return WasmJsCookieManager()
}

actual fun getCookieExpirationDate(expiresDate: Long): String {
    // TODO
    return "0"
}
