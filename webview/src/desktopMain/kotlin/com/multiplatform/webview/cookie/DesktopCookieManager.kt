package com.multiplatform.webview.cookie

import co.touchlab.kermit.Logger
import org.cef.network.CefCookie
import org.cef.network.CefCookieManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DesktopCookieManager : CookieManager {
    /**
     * CefCookieManager.getGlobalManager() is not available until CEF is initialized.
     * Thus, we can only initialize it lazily.
     */
    private var desktopCookieManager: CefCookieManager? = null

    override suspend fun setCookie(url: String, cookie: Cookie) {
        if (desktopCookieManager == null) {
            desktopCookieManager = CefCookieManager.getGlobalManager()
        }
        val currentTime = System.currentTimeMillis()
        Logger.i(tag = "DesktopCookieManager") { "DesktopCookieManager setCookie: $url, $cookie" }
        desktopCookieManager!!.setCookie(
            url, CefCookie(
                cookie.name,
                cookie.value,
                cookie.domain,
                cookie.path,
                cookie.isSecure ?: false,
                cookie.isHttpOnly ?: false,
                Date(currentTime),
                Date(currentTime),
                Date(cookie.expiresDate ?: currentTime).before(Date(currentTime)),
                Date(cookie.expiresDate ?: System.currentTimeMillis())
            )
        )
    }

    override suspend fun getCookies(url: String): List<Cookie> {
        if (desktopCookieManager == null) {
            desktopCookieManager = CefCookieManager.getGlobalManager()
        }
        Logger.i(tag = "DesktopCookieManager") { "DesktopCookieManager getCookies: $url" }
        val cookieList = mutableListOf<Cookie>()
        CefCookieManager.getGlobalManager().visitUrlCookies(
            url, true
        ) { cookie, _, _, _ ->
            cookieList.add(
                Cookie(
                    name = cookie.name,
                    value = cookie.value,
                    domain = cookie.domain,
                    path = cookie.path,
                    expiresDate = cookie.expires?.time,
                    sameSite = null,
                    isSecure = cookie.secure,
                    isHttpOnly = cookie.httponly,
                    maxAge = null
                )
            )
            true
        }

        return cookieList
    }

    override suspend fun removeAllCookies() {
        CefCookieManager.getGlobalManager().deleteCookies("", "")
    }
}

actual fun getCookieExpirationDate(expiresDate: Long): String {
    val sdf = SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("GMT")
    }
    return sdf.format(Date(expiresDate))
}

@Suppress("FunctionName") // Builder Function
actual fun WebViewCookieManager(): CookieManager = DesktopCookieManager