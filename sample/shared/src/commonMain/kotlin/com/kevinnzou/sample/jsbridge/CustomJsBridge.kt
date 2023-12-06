package com.kevinnzou.sample.jsbridge

import com.multiplatform.webview.jsbridge.JsBridge

/**
 * Created By Kevin Zou On 2023/12/6
 */
class CustomJsBridge : JsBridge() {
    init {
        register(GreetJsHandler())
    }
}