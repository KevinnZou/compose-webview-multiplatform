package com.multiplatform.webview.cookie

/**
 * Converts a timestamp to a cookie expiration date string in the browser's expected format.
 */
actual fun getCookieExpirationDate(expiresDate: Long): String = jsDateToUTCString(expiresDate)

@JsFun("timestamp => new Date(timestamp).toUTCString()")
private external fun jsDateToUTCString(timestamp: Long): String

/**
 * Sets a cookie in the browser.
 */
private fun setDocumentCookie(cookieStr: String) = setJsCookie(cookieStr)

@JsFun("cookieStr => { document.cookie = cookieStr; }")
private external fun setJsCookie(cookieStr: String)

/**
 * Gets all cookies from the browser.
 */
private fun getDocumentCookies(): String = getJsCookies()

@JsFun("() => document.cookie")
private external fun getJsCookies(): String

@Suppress(names = ["FunctionName"])
actual fun WebViewCookieManager(): CookieManager = WasmJsCookieManager

object WasmJsCookieManager : CookieManager {
    override suspend fun setCookie(
        url: String,
        cookie: Cookie,
    ) {
        // Create the cookie string
        val cookieStr =
            buildString {
                append("${cookie.name}=${cookie.value}")

                cookie.domain?.let { append("; domain=$it") }
                cookie.path?.let { append("; path=$it") }
                cookie.expiresDate?.let { append("; expires=${getCookieExpirationDate(it)}") }
                cookie.maxAge?.let { append("; max-age=$it") }
                cookie.sameSite?.let { append("; SameSite=$it") }
                if (cookie.isSecure == true) append("; Secure")
                if (cookie.isHttpOnly == true) append("; HttpOnly")
            }

        // Set the cookie using the document.cookie API
        setDocumentCookie(cookieStr)
    }

    override suspend fun getCookies(url: String): List<Cookie> {
        val cookiesStr = getDocumentCookies()
        if (cookiesStr.isEmpty()) return emptyList()

        return cookiesStr.split(";").map { cookieStr ->
            val parts = cookieStr.trim().split("=", limit = 2)
            val name = parts[0]
            val value = if (parts.size > 1) parts[1] else ""

            Cookie(
                name = name,
                value = value,
                domain = null, // These additional properties aren't accessible via document.cookie
                path = null,
                expiresDate = null,
                sameSite = null,
                isSecure = null,
                isHttpOnly = null,
                maxAge = null,
            )
        }
    }

    override suspend fun removeAllCookies() {
        val cookies = getCookies("")
        for (cookie in cookies) {
            // To delete a cookie, set it with an expired date
            val expireCookie =
                buildString {
                    append("${cookie.name}=")
                    append("; path=/")
                    append("; expires=Thu, 01 Jan 1970 00:00:00 GMT")
                }
            setDocumentCookie(expireCookie)
        }
    }

    override suspend fun removeCookies(url: String) {
        // In browser context, we can't easily remove cookies for a specific URL
        // So we'll use the same approach as removeAllCookies
        removeAllCookies()
    }
}
