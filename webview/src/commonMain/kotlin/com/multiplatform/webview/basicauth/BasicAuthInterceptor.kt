package com.multiplatform.webview.basicauth

import com.multiplatform.webview.web.WebViewNavigator

data class BasicAuthChallenge(
    val host: String,
    val realm: String? = null,
    val isProxy: Boolean = false,
    val previousFailureCount: Int = 0,
)

/**
 * Exactly one of proceed or cancel should be called.
 */
interface BasicAuthHandler {
    fun proceed(username: String, password: String)
    fun cancel()
}

interface BasicAuthInterceptor {
    fun onHttpAuthRequest(
        challenge: BasicAuthChallenge,
        handler: BasicAuthHandler,
        navigator: WebViewNavigator,
    ): Boolean
}
