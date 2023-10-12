package com.multiplatform.webview.cookie

import co.touchlab.kermit.Logger
import org.cef.callback.CefCookieVisitor
import org.cef.misc.BoolRef
import org.cef.network.CefCookie
import org.cef.network.CefCookieManager
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Desktop implementation of [CookieManager].
 */
object DesktopCookieManager : CookieManager, CefCookieVisitor {
    /**
     * CefCookieManager.getGlobalManager() is not available until CEF is initialized.
     * Thus, we can only initialize it lazily.
     */
    private var desktopCookieManager: CefCookieManager? = null
    private val cookies: MutableSet<CefCookie> = mutableSetOf()

    private fun applyManager() {
        if (desktopCookieManager == null) {
            desktopCookieManager = CefCookieManager.getGlobalManager()
        }
        desktopCookieManager?.visitAllCookies(this)
    }

    override suspend fun setCookie(url: String, cookie: Cookie) = suspendCoroutine { continuation ->
        applyManager()

        val currentTime = System.currentTimeMillis()
        val cefCookie = CefCookie(
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
        if (desktopCookieManager?.setCookie(url, cefCookie) == true) {
            visit(cefCookie, 1, 1, BoolRef())
        }
        desktopCookieManager?.flushStore {
            continuation.resume(Unit)
        } ?: continuation.resume(Unit)
    }

    override suspend fun getCookies(url: String): List<Cookie> = suspendCoroutine { continuation ->
        applyManager()

        Logger.i(tag = "DesktopCookieManager") { "DesktopCookieManager getCookies: $url" }
        val cookieList = mutableSetOf<Cookie>()
        desktopCookieManager?.visitUrlCookies(url, true) { cookie, _, _, _ ->
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

        cookies.filter {
            it.domain == URL(url).host
        }.map {
            Cookie(
                name = it.name,
                value = it.value,
                domain = it.domain,
                path = it.path,
                expiresDate = it.expires?.time,
                sameSite = null,
                isSecure = it.secure,
                isHttpOnly = it.httponly,
                maxAge = null
            )
        }.forEach(cookieList::add)

        continuation.resume(cookieList.toList())
    }

    override suspend fun removeAllCookies() = suspendCoroutine { continuation ->
        applyManager()

        cookies.clear()
        desktopCookieManager?.deleteCookies("", "")
        desktopCookieManager?.flushStore {
            continuation.resume(Unit)
        } ?: continuation.resume(Unit)
    }

    override suspend fun removeCookies(url: String) = suspendCoroutine { continuation ->
        applyManager()

        cookies.removeIf {
            it.domain == URL(url).host
        }
        desktopCookieManager?.deleteCookies(url, "")
        desktopCookieManager?.flushStore {
            continuation.resume(Unit)
        } ?: continuation.resume(Unit)
    }

    override fun visit(cookie: CefCookie?, count: Int, total: Int, delete: BoolRef?): Boolean {
        cookie?.let(cookies::add)

        return true
    }
}

actual fun getCookieExpirationDate(expiresDate: Long): String {
    val sdf = SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("GMT")
    }
    return sdf.format(Date(expiresDate))
}

/**
 * Returns an instance of [DesktopCookieManager] for Desktop.
 */
@Suppress("FunctionName") // Builder Function
actual fun WebViewCookieManager(): CookieManager = DesktopCookieManager