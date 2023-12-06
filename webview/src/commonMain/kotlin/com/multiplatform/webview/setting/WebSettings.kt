package com.multiplatform.webview.setting

import com.multiplatform.webview.util.KLogSeverity
import com.multiplatform.webview.util.KLogger

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
     * Set the zoom level of the WebView.
     * Default is 1.0.
     */
    var zoomLevel: Double = 1.0

    /**
     * Whether cross-origin requests in the context of a file scheme URL should be allowed to
     * access content from other file scheme URLs. Note that some accesses such as image HTML
     * elements don't follow same-origin rules and aren't affected by this setting.
     * <p>
     * <b>Don't</b> enable this setting if you open files that may be created or altered by
     * external sources. Enabling this setting allows malicious scripts loaded in a {@code file://}
     * context to access arbitrary local files including WebView cookies and app private data.
     * <p class="note">
     * Loading content via {@code file://} URLs is generally discouraged. See the note in
     * {@link #setAllowFileAccess}.
     * <p>
     *
     *  The default value is false.
     */
    var allowFileAccessFromFileURLs: Boolean = false

    /**
     * Whether cross-origin requests in the context of a file scheme URL should be allowed to
     * access content from <i>any</i> origin. This includes access to content from other file
     * scheme URLs or web contexts. Note that some access such as image HTML elements doesn't
     * follow same-origin rules and isn't affected by this setting.
     * <p>
     * <b>Don't</b> enable this setting if you open files that may be created or altered by
     * external sources. Enabling this setting allows malicious scripts loaded in a {@code file://}
     * context to launch cross-site scripting attacks, either accessing arbitrary local files
     * including WebView cookies, app private data or even credentials used on arbitrary web sites.
     * <p class="note">
     * Loading content via {@code file://} URLs is generally discouraged. See the note in
     * {@link #setAllowFileAccess}.
     * <p>
     *
     * The default value is false.
     */
    var allowUniversalAccessFromFileURLs: Boolean = false

    /**
     * Log severity for the WebView.
     * Default is [KLogSeverity.Info]
     */
    var logSeverity: KLogSeverity = KLogSeverity.Info
        set(value) {
            field = value
            KLogger.setMinSeverity(value)
        }

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
