package com.multiplatform.webview.cookie

import com.multiplatform.webview.util.KLogger
import dev.datlag.kcef.KCEFCookieManager
import org.cef.network.CefCookie
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DesktopCookieManager : CookieManager {
    override suspend fun setCookie(
        url: String,
        cookie: Cookie,
    ) {
        val currentTime = System.currentTimeMillis()
        val cefCookie =
            CefCookie(
                cookie.name,
                cookie.value,
                cookie.domain,
                cookie.path,
                cookie.isSecure ?: false,
                cookie.isHttpOnly ?: false,
                Date(currentTime),
                Date(currentTime),
                Date(cookie.expiresDate ?: currentTime).before(Date(currentTime)),
                Date(cookie.expiresDate ?: System.currentTimeMillis()),
            )
        val addedCookie = KCEFCookieManager.instance.setCookie(url, cefCookie)
        KLogger.d(tag = "DesktopCookieManager") { "Added Cookie: $addedCookie" }
    }

    override suspend fun getCookies(url: String): List<Cookie> {
        KLogger.d(tag = "DesktopCookieManager") { "DesktopCookieManager getCookies: $url" }

        return KCEFCookieManager.instance.getCookiesWhile(url, true).map {
            Cookie(
                name = it.name,
                value = it.value,
                domain = it.domain,
                path = it.path,
                expiresDate = it.expires?.time,
                sameSite = null,
                isSecure = it.secure,
                isHttpOnly = it.httponly,
                maxAge = null,
            )
        }
    }

    override suspend fun removeAllCookies() {
        KCEFCookieManager.instance.deleteAllCookies()
    }

    override suspend fun removeCookies(url: String) {
        KCEFCookieManager.instance.deleteCookies(url)
    }
}

actual fun getCookieExpirationDate(expiresDate: Long): String {
    val sdf =
        SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("GMT")
        }
    return sdf.format(Date(expiresDate))
}

/**
 * Returns an instance of [DesktopCookieManager] for Desktop.
 */
@Suppress("FunctionName") // Builder Function
actual fun WebViewCookieManager(): CookieManager = DesktopCookieManager
