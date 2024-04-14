package com.multiplatform.webview.cookie

object WasmCookieManager : CookieManager {
    override suspend fun setCookie(url: String, cookie: Cookie) {
        TODO("Not yet implemented")
    }

    override suspend fun getCookies(url: String): List<Cookie> {
        TODO("Not yet implemented")
    }

    override suspend fun removeAllCookies() {
        TODO("Not yet implemented")
    }

    override suspend fun removeCookies(url: String) {
        TODO("Not yet implemented")
    }

}

actual fun getCookieExpirationDate(expiresDate: Long): String {
    TODO("Not yet implemented")
}

/**
 * Returns an instance of [WasmCookieManager] for Wasm.
 */
@Suppress("FunctionName") // Builder Function
actual fun WebViewCookieManager(): CookieManager = WasmCookieManager