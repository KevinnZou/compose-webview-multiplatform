package com.multiplatform.webview.setting

/**
 * Created By Kevin Zou On 2023/9/20
 */

/**
 * Web settings for different platform
 */
class WebSettings {
    /**
     * Whether the WebView should enable JavaScript execution.
     * Default is true.
     */
    var isJavaScriptEnabled = true

    /**
     * WebView's user-agent string.
     * Default is null.
     */
    var customUserAgentString: String? = null

    /**
     * Android platform specific settings
     */
    val androidWebSettings = PlatformWebSettings.AndroidWebSettings()

    /**
     * Desktop platform specific settings
     */
    val desktopWebSettings = PlatformWebSettings.DesktopWebSettings()

    /**
     * iOS platform specific settings
     */
    val iOSWebSettings = PlatformWebSettings.IOSWebSettings

}

