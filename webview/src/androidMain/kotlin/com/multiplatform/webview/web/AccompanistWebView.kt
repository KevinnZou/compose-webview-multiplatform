package com.multiplatform.webview.web

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewFeature
import com.multiplatform.webview.basicauth.BasicAuthChallenge
import com.multiplatform.webview.basicauth.BasicAuthHandler
import com.multiplatform.webview.jsbridge.ConsoleBridge
import com.multiplatform.webview.jsbridge.WebViewJsBridge
import com.multiplatform.webview.request.WebRequest
import com.multiplatform.webview.request.WebRequestInterceptResult
import com.multiplatform.webview.setting.PlatformWebSettings
import com.multiplatform.webview.util.InternalStoragePathHandler
import com.multiplatform.webview.util.KLogger

/**
 * Created By Kevin Zou On 2023/9/5
 */

/**
 * A wrapper around the Android View WebView to provide a basic WebView composable.
 *
 * If you require more customisation you are most likely better rolling your own and using this
 * wrapper as an example.
 *
 * The WebView attempts to set the layoutParams based on the Compose modifier passed in. If it
 * is incorrectly sizing, use the layoutParams composable function instead.
 *
 * @param state The webview state holder where the Uri to load is defined.
 * @param modifier A compose modifier
 * @param captureBackPresses Set to true to have this Composable capture back presses and navigate
 * the WebView back.
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param onCreated Called when the WebView is first created, this can be used to set additional
 * settings on the WebView. WebChromeClient and WebViewClient should not be set here as they will be
 * subsequently overwritten after this lambda is called.
 * @param onDispose Called when the WebView is destroyed. Provides a bundle which can be saved
 * if you need to save and restore state in this WebView.
 * @param client Provides access to WebViewClient via subclassing
 * @param chromeClient Provides access to WebChromeClient via subclassing
 * @param factory An optional WebView factory for using a custom subclass of WebView
 * @sample com.google.accompanist.sample.webview.BasicWebViewSample
 */
@Composable
fun AccompanistWebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    webViewJsBridge: WebViewJsBridge? = null,
    consoleBridge: ConsoleBridge? = null,
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: AccompanistWebViewClient = remember { AccompanistWebViewClient() },
    chromeClient: AccompanistWebChromeClient = remember { AccompanistWebChromeClient() },
    factory: ((Context) -> WebView)? = null,
) {
    BoxWithConstraints(modifier) {
        // WebView changes it's layout strategy based on
        // it's layoutParams. We convert from Compose Modifier to
        // layout params here.
        val width =
            if (constraints.hasFixedWidth) {
                ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                ViewGroup.LayoutParams.WRAP_CONTENT
            }
        val height =
            if (constraints.hasFixedHeight) {
                ViewGroup.LayoutParams.MATCH_PARENT
            } else {
                ViewGroup.LayoutParams.WRAP_CONTENT
            }

        val layoutParams =
            FrameLayout.LayoutParams(
                width,
                height,
            )

        AccompanistWebView(
            state,
            layoutParams,
            Modifier,
            captureBackPresses,
            navigator,
            webViewJsBridge,
            consoleBridge,
            onCreated,
            onDispose,
            client,
            chromeClient,
            factory,
        )
    }
}

/**
 * A wrapper around the Android View WebView to provide a basic WebView composable.
 *
 * If you require more customisation you are most likely better rolling your own and using this
 * wrapper as an example.
 *
 * The WebView attempts to set the layoutParams based on the Compose modifier passed in. If it
 * is incorrectly sizing, use the layoutParams composable function instead.
 *
 * @param state The webview state holder where the Uri to load is defined.
 * @param layoutParams A FrameLayout.LayoutParams object to custom size the underlying WebView.
 * @param modifier A compose modifier
 * @param captureBackPresses Set to true to have this Composable capture back presses and navigate
 * the WebView back.
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param onCreated Called when the WebView is first created, this can be used to set additional
 * settings on the WebView. WebChromeClient and WebViewClient should not be set here as they will be
 * subsequently overwritten after this lambda is called.
 * @param onDispose Called when the WebView is destroyed. Provides a bundle which can be saved
 * if you need to save and restore state in this WebView.
 * @param client Provides access to WebViewClient via subclassing
 * @param chromeClient Provides access to WebChromeClient via subclassing
 * @param factory An optional WebView factory for using a custom subclass of WebView
 */
@Composable
fun AccompanistWebView(
    state: WebViewState,
    layoutParams: FrameLayout.LayoutParams,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    webViewJsBridge: WebViewJsBridge? = null,
    consoleBridge: ConsoleBridge? = null,
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: AccompanistWebViewClient = remember { AccompanistWebViewClient() },
    chromeClient: AccompanistWebChromeClient = remember { AccompanistWebChromeClient() },
    factory: ((Context) -> WebView)? = null,
) {
    val webView = state.webView
    val scope = rememberCoroutineScope()

    BackHandler(captureBackPresses && navigator.canGoBack) {
        webView?.goBack()
    }

    // Set the state of the client and chrome client
    // This is done internally to ensure they always are the same instance as the
    // parent Web composable
    client.state = state
    client.navigator = navigator
    chromeClient.state = state

    AndroidView(
        factory = { context ->
            (factory?.invoke(context) ?: WebView(context))
                .apply {
                    onCreated(this)

                    this.layoutParams = layoutParams

                    state.viewState?.let {
                        this.restoreState(it)
                    }

                    chromeClient.context = context
                    webChromeClient = chromeClient
                    webViewClient = client

                    // Avoid covering other components - map to Android View constants explicitly
                    val desiredLayerType =
                        when (state.webSettings.androidWebSettings.layerType) {
                            PlatformWebSettings.AndroidWebSettings.LayerType.NONE -> View.LAYER_TYPE_NONE
                            PlatformWebSettings.AndroidWebSettings.LayerType.SOFTWARE -> View.LAYER_TYPE_SOFTWARE
                            PlatformWebSettings.AndroidWebSettings.LayerType.HARDWARE -> View.LAYER_TYPE_HARDWARE
                            else -> View.LAYER_TYPE_HARDWARE
                        }
                    this.setLayerType(desiredLayerType, null)

                    settings.apply {
                        state.webSettings.let {
                            javaScriptEnabled = it.isJavaScriptEnabled
                            userAgentString = it.customUserAgentString
                            allowFileAccessFromFileURLs = it.allowFileAccessFromFileURLs
                            allowUniversalAccessFromFileURLs = it.allowUniversalAccessFromFileURLs
                        }

                        state.webSettings.androidWebSettings.let {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                safeBrowsingEnabled = it.safeBrowsingEnabled
                            }
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                isAlgorithmicDarkeningAllowed = it.isAlgorithmicDarkeningAllowed
                            }
                            setBackgroundColor(state.webSettings.backgroundColor.toArgb())
                            allowFileAccess = it.allowFileAccess
                            textZoom = it.textZoom
                            useWideViewPort = it.useWideViewPort
                            standardFontFamily = it.standardFontFamily
                            defaultFontSize = it.defaultFontSize
                            loadsImagesAutomatically = it.loadsImagesAutomatically
                            domStorageEnabled = it.domStorageEnabled
                            mediaPlaybackRequiresUserGesture = it.mediaPlaybackRequiresUserGesture

                            if (it.enableSandbox) {
                                client.assetLoader =
                                    WebViewAssetLoader
                                        .Builder()
                                        .addPathHandler(
                                            it.sandboxSubdomain,
                                            InternalStoragePathHandler(),
                                        ).build()
                            }
                        }
                    }
                    if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                        val nightModeFlags =
                            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
                        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
                            WebSettingsCompat.setForceDark(
                                this.settings,
                                WebSettingsCompat.FORCE_DARK_ON,
                            )
                        } else {
                            WebSettingsCompat.setForceDark(
                                this.settings,
                                WebSettingsCompat.FORCE_DARK_OFF,
                            )
                        }

                        if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY)) {
                            WebSettingsCompat.setForceDarkStrategy(
                                this.settings,
                                WebSettingsCompat.DARK_STRATEGY_WEB_THEME_DARKENING_ONLY,
                            )
                        }
                    }
                }.also {
                    val androidWebView = AndroidWebView(it, scope, webViewJsBridge, consoleBridge)
                    state.webView = androidWebView
                    webViewJsBridge?.webView = androidWebView
                }
        },
        modifier = modifier,
        onReset = {},
        onRelease = {
            onDispose(it)
        },
    )
}

/**
 * AccompanistWebViewClient
 *
 * A parent class implementation of WebViewClient that can be subclassed to add custom behaviour.
 *
 * As Accompanist Web needs to set its own web client to function, it provides this intermediary
 * class that can be overriden if further custom behaviour is required.
 */
open class AccompanistWebViewClient : WebViewClient() {
    open lateinit var state: WebViewState
        internal set
    open lateinit var navigator: WebViewNavigator
        internal set
    private var isRedirect = false

    var assetLoader: WebViewAssetLoader? = null

    override fun onPageStarted(
        view: WebView,
        url: String?,
        favicon: Bitmap?,
    ) {
        super.onPageStarted(view, url, favicon)
        KLogger.d {
            "onPageStarted: $url"
        }
        state.loadingState = LoadingState.Loading(0.0f)
        state.errorsForCurrentRequest.clear()
        state.pageTitle = null
        state.lastLoadedUrl = url
        val supportZoom = if (state.webSettings.supportZoom) "yes" else "no"

        // set scale level
        @Suppress("ktlint:standard:max-line-length")
        val script =
            "var meta = document.createElement('meta');meta.setAttribute('name', 'viewport');meta.setAttribute('content', 'width=device-width, initial-scale=${state.webSettings.zoomLevel}, maximum-scale=10.0, minimum-scale=0.1,user-scalable=$supportZoom');document.getElementsByTagName('head')[0].appendChild(meta);"
        navigator.evaluateJavaScript(script)

        // Remove tap highlight color
        val removeHighlightScript =
            """
            var style = document.createElement('style');
            style.innerHTML = '* { -webkit-tap-highlight-color: transparent; -webkit-touch-callout: none; -webkit-user-select: none; }';
            document.head.appendChild(style);
            """.trimIndent()
        navigator.evaluateJavaScript(removeHighlightScript)
    }

    override fun shouldInterceptRequest(
        view: WebView?,
        request: WebResourceRequest?,
    ): WebResourceResponse? {
        val url = request?.url
        KLogger.d { "Intercepting request for URL: $url" }
        return url?.let {
            assetLoader?.shouldInterceptRequest(it)
        } ?: super.shouldInterceptRequest(view, request)
    }

    override fun onPageFinished(
        view: WebView,
        url: String?,
    ) {
        super.onPageFinished(view, url)
        KLogger.d {
            "onPageFinished: $url"
        }
        state.loadingState = LoadingState.Finished
        state.lastLoadedUrl = url
    }

    override fun doUpdateVisitedHistory(
        view: WebView,
        url: String?,
        isReload: Boolean,
    ) {
        KLogger.d {
            "doUpdateVisitedHistory: $url"
        }
        super.doUpdateVisitedHistory(view, url, isReload)

        navigator.canGoBack = view.canGoBack()
        navigator.canGoForward = view.canGoForward()
    }

    override fun onReceivedError(
        view: WebView,
        request: WebResourceRequest?,
        error: WebResourceError?,
    ) {
        super.onReceivedError(view, request, error)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            KLogger.e {
                "onReceivedError: $error"
            }
            return
        }
        KLogger.e {
            "onReceivedError: ${error?.description}"
        }
        if (error != null) {
            state.errorsForCurrentRequest.add(
                WebViewError(
                    code = error.errorCode,
                    description = error.description.toString(),
                    isFromMainFrame = request?.isForMainFrame ?: false,
                ),
            )
        }
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?,
    ): Boolean {
        KLogger.d {
            "shouldOverrideUrlLoading: ${request?.url} ${request?.isForMainFrame} ${request?.isRedirect} ${request?.method}"
        }
        if (isRedirect || request == null || navigator.requestInterceptor == null) {
            isRedirect = false
            return super.shouldOverrideUrlLoading(view, request)
        }
        val isRedirectRequest =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                request.isRedirect
            } else {
                false
            }
        val webRequest =
            WebRequest(
                request.url.toString(),
                request.requestHeaders?.toMutableMap() ?: mutableMapOf(),
                request.isForMainFrame,
                isRedirectRequest,
                request.method ?: "GET",
            )
        val interceptResult =
            navigator.requestInterceptor!!.onInterceptUrlRequest(
                webRequest,
                navigator,
            )
        return when (interceptResult) {
            is WebRequestInterceptResult.Allow -> {
                false
            }

            is WebRequestInterceptResult.Reject -> {
                true
            }

            is WebRequestInterceptResult.Modify -> {
                isRedirect = true
                interceptResult.request.apply {
                    navigator.stopLoading()
                    navigator.loadUrl(this.url, this.headers)
                }
                true
            }
        }
    }

    override fun onReceivedHttpAuthRequest(
        view: WebView?,
        handler: android.webkit.HttpAuthHandler?,
        host: String?,
        realm: String?
    ) {
        val interceptor = navigator.basicAuthInterceptor
        if (interceptor == null || handler == null || host == null) {
            super.onReceivedHttpAuthRequest(view, handler, host, realm)
            return
        }
        val challenge = BasicAuthChallenge(
            host = host,
            realm = realm,
            isProxy = false,
            previousFailureCount = try {
                handler.useHttpAuthUsernamePassword(); 0
            } catch (_: Throwable) {
                0
            }
        )
        val wrapped = object : BasicAuthHandler {
            private var used = false
            override fun proceed(username: String, password: String) {
                if (used) return; used = true
                handler.proceed(username, password)
            }
            override fun cancel() {
                if (used) return; used = true
                handler.cancel()
            }
        }
        val stop = try { interceptor.onHttpAuthRequest(challenge, wrapped, navigator) } catch (_: Throwable) { false }
        if (!stop) {
            super.onReceivedHttpAuthRequest(view, handler, host, realm)
        }
    }
}

/**
 * AccompanistWebChromeClient
 *
 * A parent class implementation of WebChromeClient that can be subclassed to add custom behaviour.
 *
 * As Accompanist Web needs to set its own web client to function, it provides this intermediary
 * class that can be overriden if further custom behaviour is required.
 */
open class AccompanistWebChromeClient : WebChromeClient() {
    open lateinit var state: WebViewState
        internal set
    lateinit var context: Context
        internal set
    private var lastLoadedUrl = ""

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        try {
            val msg = consoleMessage ?: return super.onConsoleMessage(consoleMessage)
            val level =
                when (msg.messageLevel()) {
                    ConsoleMessage.MessageLevel.ERROR -> "error"
                    ConsoleMessage.MessageLevel.WARNING -> "warn"
                    ConsoleMessage.MessageLevel.LOG -> "log"
                    ConsoleMessage.MessageLevel.TIP -> "info"
                    ConsoleMessage.MessageLevel.DEBUG -> "debug"
                    else -> "log"
                }
            val timestamp =
                try {
                    val sdf =
                        java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    sdf.format(java.util.Date())
                } catch (_: Throwable) {
                    System.currentTimeMillis().toString()
                }
            val iwv = state.webView
            val bridge = iwv?.consoleBridge
            bridge?.emitFromPlatform(
                level = level,
                content = msg.message(),
                sourceId = msg.sourceId(),
                lineNumber = msg.lineNumber(),
                timestamp = timestamp,
            )
        } catch (_: Exception) {
        }
        return super.onConsoleMessage(consoleMessage)
    }

    override fun onReceivedTitle(
        view: WebView,
        title: String?,
    ) {
        super.onReceivedTitle(view, title)
        KLogger.d {
            "onReceivedTitle: $title url:${view.url}"
        }
        state.pageTitle = title
        state.lastLoadedUrl = view.url ?: ""
    }

    override fun onReceivedIcon(
        view: WebView,
        icon: Bitmap?,
    ) {
        super.onReceivedIcon(view, icon)
//        state.pageIcon = icon
    }

    override fun onProgressChanged(
        view: WebView,
        newProgress: Int,
    ) {
        super.onProgressChanged(view, newProgress)
        if (state.loadingState is LoadingState.Finished && view.url == lastLoadedUrl) return
        state.loadingState =
            if (newProgress == 100) {
                LoadingState.Finished
            } else {
                LoadingState.Loading(newProgress / 100.0f)
            }
        lastLoadedUrl = view.url ?: ""
    }

    override fun onPermissionRequest(request: PermissionRequest) {
        val grantedPermissions = mutableListOf<String>()
        KLogger.d { "onPermissionRequest received request for resources [${request.resources}]" }

        request.resources.forEach { resource ->
            var androidPermission: String? = null

            when (resource) {
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> {
                    androidPermission = android.Manifest.permission.RECORD_AUDIO
                }

                PermissionRequest.RESOURCE_MIDI_SYSEX -> {
                    // MIDI sysex is only available on Android M and above
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (state.webSettings.androidWebSettings.allowMidiSysexMessages) {
                            grantedPermissions.add(PermissionRequest.RESOURCE_MIDI_SYSEX)
                        }
                    }
                }

                PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID -> {
                    if (state.webSettings.androidWebSettings.allowProtectedMedia) {
                        grantedPermissions.add(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID)
                    }
                }

                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> {
                    androidPermission = android.Manifest.permission.CAMERA
                }
            }

            if (androidPermission != null) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        androidPermission,
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    grantedPermissions.add(resource)
                    KLogger.d {
                        "onPermissionRequest permission [$androidPermission] was already granted for resource [$resource]"
                    }
                } else {
                    KLogger.w {
                        "onPermissionRequest didn't find already granted permission [$androidPermission] for resource [$resource]"
                    }
                }
            }
        }

        if (grantedPermissions.isNotEmpty()) {
            request.grant(grantedPermissions.toTypedArray())
            KLogger.d { "onPermissionRequest granted permissions: ${grantedPermissions.joinToString()}" }
        } else {
            request.deny()
            KLogger.d { "onPermissionRequest denied permissions: ${request.resources}" }
        }
    }

    override fun getDefaultVideoPoster(): Bitmap? =
        when {
            state.webSettings.androidWebSettings.hideDefaultVideoPoster -> createBitmap(50, 50)
            else -> super.getDefaultVideoPoster()
        }
}
