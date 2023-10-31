package com.multiplatform.webview.cookie

import androidx.webkit.CookieManagerCompat
import androidx.webkit.WebViewFeature
import com.multiplatform.webview.util.KLogger
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Android implementation of [CookieManager].
 */
object AndroidCookieManager : CookieManager {
    private val androidCookieManager = android.webkit.CookieManager.getInstance()

    override suspend fun setCookie(
        url: String,
        cookie: Cookie,
    ) {
        androidCookieManager.setCookie(url, cookie.toString())
    }

    override suspend fun getCookies(url: String): List<Cookie> {
        val cookieList = mutableListOf<Cookie>()

        var cookies: List<String> = ArrayList()

        if (WebViewFeature.isFeatureSupported(WebViewFeature.GET_COOKIE_INFO)) {
            cookies =
                CookieManagerCompat.getCookieInfo(
                    androidCookieManager, url,
                )
        } else {
            val cookiesString: String? = androidCookieManager.getCookie(url)
            if (!cookiesString.isNullOrBlank()) {
                cookies =
                    cookiesString.split("; ".toRegex())
                        .dropLastWhile { it.isEmpty() }
            }
        }

        for (cookie in cookies) {
            val cookieParams =
                cookie.split(";".toRegex())
                    .dropLastWhile { it.isEmpty() }

            if (cookieParams.isEmpty()) continue

            val nameValue = cookieParams[0].split("=".toRegex(), limit = 2).toTypedArray()
            val name = nameValue[0].trim { it <= ' ' }
            val value = if (nameValue.size > 1) nameValue[1].trim { it <= ' ' } else ""
            var cookieObj =
                Cookie(
                    name = name,
                    value = value,
                    domain = null,
                    path = null,
                    expiresDate = null,
                    isSessionOnly = false,
                    sameSite = null,
                    isSecure = null,
                    isHttpOnly = null,
                    maxAge = null,
                )

            if (WebViewFeature.isFeatureSupported(WebViewFeature.GET_COOKIE_INFO)) {
                cookieObj =
                    cookieObj.copy(
                        isSecure = false,
                        isHttpOnly = false,
                    )

                for (i in 1 until cookieParams.size) {
                    val cookieParamNameValue =
                        cookieParams[i].split("=".toRegex(), limit = 2).toTypedArray()
                    val cookieParamName = cookieParamNameValue[0].trim { it <= ' ' }
                    val cookieParamValue =
                        if (cookieParamNameValue.size > 1) cookieParamNameValue[1].trim { it <= ' ' } else ""

                    when {
                        cookieParamName.equals("Expires", ignoreCase = true) -> {
                            try {
                                val sdf = SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.US)
                                val expiryDate = sdf.parse(cookieParamValue)
                                if (expiryDate != null) {
                                    cookieObj =
                                        cookieObj.copy(
                                            expiresDate = expiryDate.time,
                                        )
                                }
                            } catch (e: ParseException) {
                                e.printStackTrace()
                            }
                        }

                        cookieParamName.equals("Max-Age", ignoreCase = true) -> {
                            try {
                                val maxAge = cookieParamValue.toLong()
                                cookieObj =
                                    cookieObj.copy(
                                        maxAge = maxAge,
                                        expiresDate = System.currentTimeMillis() + maxAge,
                                    )
                            } catch (e: NumberFormatException) {
                                e.printStackTrace()
                            }
                        }

                        cookieParamName.equals("Domain", ignoreCase = true) -> {
                            cookieObj = cookieObj.copy(domain = cookieParamValue)
                        }

                        cookieParamName.equals("SameSite", ignoreCase = true) -> {
                            runCatching { Cookie.HTTPCookieSameSitePolicy.valueOf(cookieParamValue) }
                                .onSuccess { cookieObj = cookieObj.copy(sameSite = it) }
                        }

                        cookieParamName.equals("Secure", ignoreCase = true) -> {
                            cookieObj = cookieObj.copy(isSecure = true)
                        }

                        cookieParamName.equals("HttpOnly", ignoreCase = true) -> {
                            cookieObj = cookieObj.copy(isHttpOnly = true)
                        }

                        cookieParamName.equals("Path", ignoreCase = true) -> {
                            cookieObj = cookieObj.copy(path = cookieParamValue)
                        }
                    }
                }
            }
            cookieList.add(cookieObj)
        }
        return cookieList
    }

    override suspend fun removeAllCookies() {
        androidCookieManager.removeAllCookies {
            KLogger.d {
                "AndroidCookieManager: removeAllCookies: $it"
            }
        }
        androidCookieManager.flush()
    }

    /**
     * Not supported on Android yet.
     */
    override suspend fun removeCookies(url: String) {
        // TODO
    }
}

actual fun getCookieExpirationDate(expiresDate: Long): String {
    val sdf =
        SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("GMT")
        }
    return sdf.format(Date(expiresDate))
}

@Suppress("FunctionName") // Builder Function
actual fun WebViewCookieManager(): CookieManager = AndroidCookieManager
