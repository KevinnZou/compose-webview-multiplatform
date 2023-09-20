package com.multiplatform.webview.web

/**
 * Created By Kevin Zou On 2023/9/20
 */
class WebSettings {
    var isJavaScriptEnabled = true

    val androidWebSettings = PlatformWebSettings.AndroidWebSettings()
    val desktopWebSettings = PlatformWebSettings.DesktopWebSettings
    val iOSWebSettings = PlatformWebSettings.IOSWebSettings


    sealed class PlatformWebSettings {
        data class AndroidWebSettings(
            var isAlgorithmicDarkeningAllowed: Boolean = false,
            var safeBrowsingEnabled: Boolean = false
        ): PlatformWebSettings() {

        }
        data object DesktopWebSettings: PlatformWebSettings()
        data object IOSWebSettings: PlatformWebSettings()
    }
}

