package com.multiplatform.webview.web

import com.multiplatform.webview.setting.PlatformWebSettings

/**
 * Created By Kevin Zou On 2023/9/20
 */
class WebSettings {
    var isJavaScriptEnabled = true

    /**
     * Android platform specific settings
     */
    val androidWebSettings = PlatformWebSettings.AndroidWebSettings()

    /**
     * Desktop platform specific settings
     */
    val desktopWebSettings = PlatformWebSettings.DesktopWebSettings

    /**
     * iOS platform specific settings
     */
    val iOSWebSettings = PlatformWebSettings.IOSWebSettings

}

