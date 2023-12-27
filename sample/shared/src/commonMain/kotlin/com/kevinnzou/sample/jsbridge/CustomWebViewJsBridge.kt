package com.kevinnzou.sample.jsbridge

import com.multiplatform.webview.jsbridge.WebViewJsBridge

/**
 * Created By Kevin Zou On 2023/12/6
 */
class CustomWebViewJsBridge : WebViewJsBridge() {
    init {
        register(GreetJsMessageHandler())
    }
}
