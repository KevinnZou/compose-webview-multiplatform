package com.multiplatform.webview.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.multiplatform.webview.jsbridge.ConsoleBridge
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import com.multiplatform.webview.request.WKSchemeHandler
import com.multiplatform.webview.util.toUIColor
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSOperatingSystemVersion
import platform.Foundation.NSProcessInfo
import platform.Foundation.setValue
import platform.WebKit.WKAudiovisualMediaTypeAll
import platform.WebKit.WKAudiovisualMediaTypeNone
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration
import platform.WebKit.javaScriptEnabled

/**
 * iOS WebView implementation.
 */
@Composable
actual fun ActualWebView(
    state: WebViewState,
    modifier: Modifier,
    captureBackPresses: Boolean,
    navigator: WebViewNavigator,
    webViewJsBridge: WebViewJsBridge?,
    consoleBridge: ConsoleBridge?,
    onCreated: (NativeWebView) -> Unit,
    onDispose: (NativeWebView) -> Unit,
    platformWebViewParams: PlatformWebViewParams?,
    factory: (WebViewFactoryParam) -> NativeWebView,
) {
    IOSWebView(
        state = state,
        modifier = modifier,
        captureBackPresses = captureBackPresses,
        navigator = navigator,
        webViewJsBridge = webViewJsBridge,
        onCreated = onCreated,
        onDispose = onDispose,
        platformWebViewParams = platformWebViewParams,
        factory = factory,
    )
}

/** iOS WebView factory parameters: configuration created from WebSettings. */
actual data class WebViewFactoryParam(
    val config: WKWebViewConfiguration,
)

/**
 * iOS-specific WebView parameters.
 *
 * @param customSchemes List of custom URL schemes to register at WebView creation time
 *                      (for example, "app", "local"). These schemes are added to the
 *                      underlying [WKWebViewConfiguration] when the WebView is created
 *                      and cannot be added to or removed from an existing WebView instance.
 *
 *                      Requests to these schemes will be handled by the RequestInterceptor,
 *                      which should return [WebRequestInterceptResult.Respond] with the
 *                      response data.
 *
 *                      Note: WKWebView does not allow certain built-in schemes such as
 *                      "http", "https", "file", "ftp", "about", "data", or "javascript"
 *                      to be used as custom schemes. These reserved schemes will be
 *                      automatically filtered out and not registered.
 */
actual class PlatformWebViewParams(
    val customSchemes: List<String> = emptyList(),
)

/** Default WebView factory for iOS. */
@OptIn(ExperimentalForeignApi::class)
actual fun defaultWebViewFactory(param: WebViewFactoryParam) =
    WKWebView(
        frame = CGRectZero.readValue(),
        configuration = param.config,
    )

/**
 * iOS WebView implementation.
 */
@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
fun IOSWebView(
    state: WebViewState,
    modifier: Modifier,
    captureBackPresses: Boolean,
    navigator: WebViewNavigator,
    webViewJsBridge: WebViewJsBridge?,
    onCreated: (NativeWebView) -> Unit,
    onDispose: (NativeWebView) -> Unit,
    platformWebViewParams: PlatformWebViewParams?,
    factory: (WebViewFactoryParam) -> NativeWebView,
) {
    val observer =
        remember {
            WKWebViewObserver(
                state = state,
                navigator = navigator,
            )
        }
    val navigationDelegate = remember { WKNavigationDelegate(state, navigator) }
    // Recreate scheme handler if navigator changes to avoid stale state
    val schemeHandler = remember(navigator) { WKSchemeHandler(navigator) }
    val scope = rememberCoroutineScope()

    UIKitView(
        factory = {
            val config =
                WKWebViewConfiguration().apply {
                    allowsInlineMediaPlayback = true
                    mediaTypesRequiringUserActionForPlayback =
                        if (state.webSettings.iOSWebSettings.mediaPlaybackRequiresUserGesture) {
                            WKAudiovisualMediaTypeAll
                        } else {
                            WKAudiovisualMediaTypeNone
                        }
                    defaultWebpagePreferences.allowsContentJavaScript =
                        state.webSettings.isJavaScriptEnabled
                    preferences.apply {
                        setValue(
                            state.webSettings.allowFileAccessFromFileURLs,
                            forKey = "allowFileAccessFromFileURLs",
                        )
                        javaScriptEnabled = state.webSettings.isJavaScriptEnabled
                    }
                    setValue(
                        value = state.webSettings.allowUniversalAccessFromFileURLs,
                        forKey = "allowUniversalAccessFromFileURLs",
                    )

                    // Register custom URL scheme handlers
                    // Filter out reserved schemes that WKWebView doesn't allow
                    val reservedSchemes =
                        setOf(
                            "http",
                            "https",
                            "file",
                            "ftp",
                            "about",
                            "data",
                            "javascript",
                        )
                    platformWebViewParams
                        ?.customSchemes
                        ?.filter { scheme ->
                            val normalized = scheme.lowercase()
                            val isReserved = normalized in reservedSchemes
                            if (isReserved) {
                                println("WKWebView: Skipping registration of reserved URL scheme: $scheme")
                            }
                            !isReserved
                        }?.forEach { scheme ->
                            setURLSchemeHandler(schemeHandler, forURLScheme = scheme)
                        }
                }
            factory(WebViewFactoryParam(config))
                .apply {
                    onCreated(this)
                    state.viewState?.let {
                        this.interactionState = it
                    }
                    allowsBackForwardNavigationGestures = captureBackPresses
                    customUserAgent = state.webSettings.customUserAgentString
                    this.addProgressObservers(
                        observer = observer,
                    )
                    this.navigationDelegate = navigationDelegate

                    state.webSettings.let {
                        val backgroundColor =
                            (it.iOSWebSettings.backgroundColor ?: it.backgroundColor).toUIColor()
                        val scrollViewColor =
                            (
                                it.iOSWebSettings.underPageBackgroundColor
                                    ?: it.backgroundColor
                            ).toUIColor()
                        setOpaque(it.iOSWebSettings.opaque)
                        if (!it.iOSWebSettings.opaque) {
                            setBackgroundColor(backgroundColor)
                            scrollView.setBackgroundColor(scrollViewColor)
                        }
                        scrollView.pinchGestureRecognizer?.enabled = it.supportZoom
                    }
                    state.webSettings.iOSWebSettings.let {
                        with(scrollView) {
                            bounces = it.bounces
                            scrollEnabled = it.scrollEnabled
                            showsHorizontalScrollIndicator = it.showHorizontalScrollIndicator
                            showsVerticalScrollIndicator = it.showVerticalScrollIndicator
                            contentInsetAdjustmentBehavior =
                                platform.UIKit.UIScrollViewContentInsetAdjustmentBehavior.UIScrollViewContentInsetAdjustmentNever
                        }
                    }

                    /**
                     * Sets the inspectable property of the WKWebView.
                     * This is only done if the operating system version is iOS 16.4 or later
                     * to prevent crashes on lower versions where the `setInspectable` method is not available.
                     * Enabling this allows Safari Web Inspector to debug the content of the WebView.
                     * The value is determined by `state.webSettings.iOSWebSettings.isInspectable`.
                     */
                    val minSetInspectableVersion =
                        cValue<NSOperatingSystemVersion> {
                            majorVersion = 16
                            minorVersion = 4
                            patchVersion = 0
                        }
                    if (NSProcessInfo.processInfo.isOperatingSystemAtLeastVersion(minSetInspectableVersion)) {
                        this.setInspectable(state.webSettings.iOSWebSettings.isInspectable)
                    }
                }.also {
                    val iosWebView = IOSWebView(it, scope, webViewJsBridge)
                    state.webView = iosWebView
                    webViewJsBridge?.webView = iosWebView
                }
        },
        modifier = modifier,
        onRelease = {
            state.webView = null
            it.removeProgressObservers(
                observer = observer,
            )
            it.navigationDelegate = null
            onDispose(it)
        },
        properties =
            UIKitInteropProperties(
                interactionMode =
                    if (state.webSettings.iOSWebSettings.scrollEnabled) {
                        UIKitInteropInteractionMode.NonCooperative
                    } else {
                        UIKitInteropInteractionMode.Cooperative()
                    },
                isNativeAccessibilityEnabled = true,
            ),
    )
}
