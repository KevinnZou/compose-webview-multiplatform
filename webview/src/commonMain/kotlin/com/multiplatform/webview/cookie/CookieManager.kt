package com.multiplatform.webview.cookie


/**
 * Cookie Manager exposing access to cookies of the WebView.
 * This is an interface to allow platform specific implementations.
 * ---------------------------------------------------------------
 * PS: Not having it as expect/actual class was a conscious decision,
 * since expect/actual classes will be marked as beta in coming kotlin releases.
 * */
interface CookieManager {
    fun setCookie(url: String, cookie: Cookie)
    fun getCookies(url: String): List<Cookie>
    fun removeAllCookies()
}

@Suppress("FunctionName") // Builder Function
expect fun ActualCookieManager(): CookieManager