# Setting
Starting from version 1.3.0, this library allows users to customize web settings.

There are some common [WebSettings](https://github.com/KevinnZou/compose-webview-multiplatform/blob/main/webview/src/commonMain/kotlin/com/multiplatform/webview/setting/WebSettings.kt) 
that can be shared across different platforms, such as `isJavaScriptEnabled` and `userAgent`.

## WebSettings

```kotlin
class WebSettings {
  var isJavaScriptEnabled = true

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
```

## PlatformWebSettings

For platform specific settings, this library provides the [PlatformWebSettings](https://github.com/KevinnZou/compose-webview-multiplatform/blob/main/webview/src/commonMain/kotlin/com/multiplatform/webview/setting/PlatformWebSettings.kt).

These settings will only be applied to the respective platforms and do not affect other platforms.
```kotlin
sealed class PlatformWebSettings {
    data class AndroidWebSettings(
        /**
         * whether the WebView should support zooming using its on-screen zoom
         * controls and gestures. The particular zoom mechanisms that should be used
         * can be set with {@link #setBuiltInZoomControls}. This setting does not
         * affect zooming performed using the {@link WebView#zoomIn()} and
         * {@link WebView#zoomOut()} methods. The default is {@code true}.
         *
         * @param support whether the WebView should support zoom
         */
        var supportZoom: Boolean = true,

        /**
         * whether Safe Browsing is enabled. Safe Browsing allows WebView to
         * protect against malware and phishing attacks by verifying the links.
         */
        var safeBrowsingEnabled: Boolean = true,

        // .....
    ) : PlatformWebSettings()

    data class DesktopWebSettings(
        var offScreenRendering: Boolean = false,
        var transparent: Boolean = false,
    ) : PlatformWebSettings()

    data object IOSWebSettings : PlatformWebSettings()
}
```

## Usage
Developers can configure custom settings in the shared code in the following way:
```kotlin
val webViewState = rememberWebViewStateWithHTMLData(
    data = html
)
DisposableEffect(Unit) {
    webViewState.webSettings.apply {
        isJavaScriptEnabled = true
        androidWebSettings.apply {
            isAlgorithmicDarkeningAllowed = true
            safeBrowsingEnabled = true
        }
    }
    onDispose { }
}
```