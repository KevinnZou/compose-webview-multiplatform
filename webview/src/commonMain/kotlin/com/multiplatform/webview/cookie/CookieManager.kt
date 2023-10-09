package com.multiplatform.webview.cookie


/**
 * Cookie Manager exposing access to cookies of the WebView.
 * This is an interface to allow platform specific implementations.
 * ---------------------------------------------------------------
 * PS: Not having it as expect/actual class was a conscious decision,
 * since expect/actual classes will be marked as beta in coming kotlin releases.
 * */
interface CookieManager {
    suspend fun setCookie(url: String, cookie: Cookie)
    suspend fun getCookies(url: String): List<Cookie>
    suspend fun removeAllCookies()
    suspend fun removeCookies(url: String)
}

@Suppress("FunctionName") // Builder Function
expect fun WebViewCookieManager(): CookieManager