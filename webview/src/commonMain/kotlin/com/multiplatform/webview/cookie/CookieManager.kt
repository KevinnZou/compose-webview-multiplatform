package com.multiplatform.webview.cookie

/**
 * Cookie Manager exposing access to cookies of the WebView.
 * This is an interface to allow platform specific implementations.
 * ---------------------------------------------------------------
 * PS: Not having it as expect/actual class was a conscious decision,
 * since expect/actual classes will be marked as beta in coming kotlin releases.
 * */
interface CookieManager {
    /**
     * Sets a cookie for the given url.
     * @param url The url for which the cookie is to be set.
     * @param cookie The cookie to be set.
     * */
    suspend fun setCookie(
        url: String,
        cookie: Cookie,
    )

    /**
     * Gets all the cookies for the given url.
     * @param url The url for which the cookies are to be retrieved.
     *
     * @return A list of cookies for the given url.
     * */
    suspend fun getCookies(url: String): List<Cookie>

    /**
     * Removes all the cookies.
     * */
    suspend fun removeAllCookies()

    /**
     * Removes all the cookies for the given url.
     * @param url The url for which the cookies are to be removed.
     * */
    suspend fun removeCookies(url: String)
}

/**
 * Creates a [CookieManager] instance.
 */
@Suppress("FunctionName") // Builder Function
expect fun WebViewCookieManager(): CookieManager
