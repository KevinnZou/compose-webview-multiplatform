package com.multiplatform.webview.web

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.ViewGroup
import android.webkit.GeolocationPermissions
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.multiplatform.webview.util.KLogger
import com.multiplatform.webview.web.PermissionRequest as MultiplatformPermissionRequest

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
    onCreated: (WebView) -> Unit = {},
    onDispose: (WebView) -> Unit = {},
    client: AccompanistWebViewClient = remember { AccompanistWebViewClient() },
    chromeClient: AccompanistWebChromeClient = remember { AccompanistWebChromeClient() },
    factory: ((Context) -> WebView)? = null,
) {
    val webView = state.webView

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
            (factory?.invoke(context) ?: WebView(context)).apply {
                onCreated(this)

                this.layoutParams = layoutParams

//                state.viewState?.let {
//                    this.restoreState(it)
//                }

                webChromeClient = chromeClient
                webViewClient = client
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
                        setSupportZoom(it.supportZoom)
                        allowFileAccess = it.allowFileAccess
                        textZoom = it.textZoom
                        useWideViewPort = it.useWideViewPort
                        standardFontFamily = it.standardFontFamily
                        defaultFontSize = it.defaultFontSize
                        loadsImagesAutomatically = it.loadsImagesAutomatically
                        domStorageEnabled = it.domStorageEnabled
                    }
                }
            }.also { state.webView = AndroidWebView(it) }
        },
        modifier = modifier,
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
//        state.pageIcon = null

        state.lastLoadedUrl = url
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
    }

    override fun doUpdateVisitedHistory(
        view: WebView,
        url: String?,
        isReload: Boolean,
    ) {
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
        KLogger.e {
            "onReceivedError: ${error?.description}"
        }
        if (error != null) {
            state.errorsForCurrentRequest.add(
                WebViewError(
                    error.errorCode,
                    error.description.toString(),
                ),
            )
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
open class AccompanistWebChromeClient(
    private val permissionHandler: PermissionHandler = { PermissionRequestResponse.DENY }
) : WebChromeClient() {
    open lateinit var state: WebViewState
        internal set

    override fun onPermissionRequest(request: PermissionRequest) {
        val response = permissionHandler(request.toMultiplatformPermissionRequest())

        if (response == PermissionRequestResponse.GRANT) {
            request.grant(request.resources)
        } else {
            request.deny()
        }
    }

    override fun onGeolocationPermissionsShowPrompt(
        origin: String,
        callback: GeolocationPermissions.Callback,
    ) {
        val allow = permissionHandler(
            MultiplatformPermissionRequest(
                origin,
                listOf(MultiplatformPermissionRequest.Permission.LOCATION)
            )
        ) == PermissionRequestResponse.GRANT

        callback.invoke(origin, allow, false)
    }

    override fun onReceivedTitle(view: WebView, title: String?) {
        super.onReceivedTitle(view, title)
        KLogger.d {
            "onReceivedTitle: $title"
        }
        state.pageTitle = title
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
        if (state.loadingState is LoadingState.Finished) return
        state.loadingState = LoadingState.Loading(newProgress / 100.0f)
    }
}

private fun PermissionRequest.toMultiplatformPermissionRequest() = MultiplatformPermissionRequest(
    origin.toString(),
    resources.map(String::toPermission)
)

private fun String.toPermission(): MultiplatformPermissionRequest.Permission = when (this) {
    PermissionRequest.RESOURCE_AUDIO_CAPTURE -> MultiplatformPermissionRequest.Permission.AUDIO
    PermissionRequest.RESOURCE_MIDI_SYSEX -> MultiplatformPermissionRequest.Permission.MIDI
    PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID -> MultiplatformPermissionRequest.Permission.MEDIA
    PermissionRequest.RESOURCE_VIDEO_CAPTURE -> MultiplatformPermissionRequest.Permission.VIDEO
    else -> error("Unknown resource: $this")
}
