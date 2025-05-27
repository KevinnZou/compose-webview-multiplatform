package com.multiplatform.webview.web

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateObserver
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.round
import kotlinx.browser.document
import kotlinx.coroutines.launch
import org.w3c.dom.Element
import org.w3c.dom.events.Event

/**
 * A Composable that renders HTML content using an iframe
 *
 * @param state The state of the HTML view
 * @param modifier The modifier for this composable
 * @param navigator The navigator for HTML navigation events
 * @param onCreated Callback invoked when the view is created
 * @param onDispose Callback invoked when the view is disposed
 */
@Composable
fun HtmlView(
    state: HtmlViewState,
    modifier: Modifier = Modifier,
    navigator: HtmlViewNavigator = rememberHtmlViewNavigator(),
    onCreated: (Element) -> Unit = {},
    onDispose: (Element) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val element = remember { mutableStateOf<Element?>(null) }
    val root = LocalLayerContainer.current
    val density = LocalDensity.current.density
    val focusManager = LocalFocusManager.current

    val componentInfo = remember { ComponentInfo<Element>() }
    val focusSwitcher = remember { FocusSwitcher(componentInfo, focusManager) }
    val eventsInitialized = remember { mutableStateOf(false) }
    val componentReady = remember { mutableStateOf(false) }

    Box(
        modifier =
            modifier.onGloballyPositioned { coordinates ->
                val location = coordinates.positionInWindow().round()
                val size = coordinates.size
                if (componentReady.value) {
                    changeCoordinates(
                        componentInfo.component,
                        size.width / density,
                        size.height / density,
                        location.x / density,
                        location.y / density,
                    )
                }
            },
    ) {
        focusSwitcher.Content()
    }

    DisposableEffect(Unit) {
        componentInfo.container = document.createElement("div")
        componentInfo.component = document.createElement("iframe")
        componentReady.value = true

        (root as Element).insertBefore(componentInfo.container, (root as Element).firstChild)
        componentInfo.container.append(componentInfo.component)

        initializingElement(componentInfo.component)
        element.value = componentInfo.component
        state.htmlElement = componentInfo.component
        onCreated(componentInfo.component)

        componentInfo.updater =
            Updater(componentInfo.component) { iframe ->
                if (!eventsInitialized.value) {
                    eventsInitialized.value = true

                    val loadCallback: (Event) -> Unit = {
                        state.loadingState = HtmlLoadingState.Finished()

                        try {
                            val title = getIframeTitleJs(iframe)
                            if (title != null) {
                                state.pageTitle = title
                            }

                            val href = getIframeUrlJs(iframe)
                            if (href != null && href != "about:blank") {
                                state.lastLoadedUrl = href
                            }
                        } catch (e: Exception) {
                            // Cross-origin restrictions might prevent access
                        }
                    }

                    val errorCallback: (Event) -> Unit = {
                        state.loadingState =
                            HtmlLoadingState.Finished(
                                isError = true,
                                errorMessage = "Failed to load content",
                            )
                    }

                    iframe.addEventListener("load", loadCallback)
                    iframe.addEventListener("error", errorCallback)

                    scope.launch {
                        navigator.handleNavigationEvents(iframe)
                    }
                }

                when (val content = state.content) {
                    is HtmlContent.Url -> {
                        setUrlJs(iframe, content.url)
                        state.loadingState = HtmlLoadingState.Loading
                    }

                    is HtmlContent.Data -> {
                        setHtmlContentJs(iframe, content.data)
                        state.loadingState = HtmlLoadingState.Loading
                        addContentIdentifierJs(iframe)
                    }

                    is HtmlContent.Post -> {
                        // POST requests not directly supported in iframe
                    }

                    HtmlContent.NavigatorOnly -> {
                        // No content update needed
                    }
                }

                setStyleJs(iframe, "border", "none")
                setStyleJs(iframe, "width", "100%")
                setStyleJs(iframe, "height", "100%")
                setStyleJs(iframe, "overflow", "auto")
            }

        onDispose {
            (root as Element).removeChild(componentInfo.container)
            componentInfo.updater.dispose()
            element.value?.let { onDispose(it) }
            state.htmlElement = null
            state.loadingState = HtmlLoadingState.Initializing
        }
    }

    SideEffect {
        if (element.value != null) {
            componentInfo.updater.update(componentInfo.component)
        }
    }
}

/**
 * Helper class to manage component information
 */
class ComponentInfo<T : Element> {
    lateinit var container: Element
    lateinit var component: T
    lateinit var updater: Updater<T>
}

/**
 * Helper class to manage focus switching
 */
class FocusSwitcher<T : Element>(
    private val info: ComponentInfo<T>,
    private val focusManager: FocusManager,
) {
    private val backwardRequester = FocusRequester()
    private val forwardRequester = FocusRequester()
    private var isRequesting = false

    fun moveBackward() {
        try {
            isRequesting = true
            backwardRequester.requestFocus()
        } finally {
            isRequesting = false
        }
        focusManager.moveFocus(FocusDirection.Previous)
    }

    fun moveForward() {
        try {
            isRequesting = true
            forwardRequester.requestFocus()
        } finally {
            isRequesting = false
        }
        focusManager.moveFocus(FocusDirection.Next)
    }

    @Composable
    fun Content() {
        Box(
            Modifier
                .focusRequester(backwardRequester)
                .onFocusChanged {
                    if (it.isFocused && !isRequesting) {
                        focusManager.clearFocus(force = true)
                        val component = info.container.firstElementChild
                        if (component != null) {
                            requestFocus(component)
                        } else {
                            moveForward()
                        }
                    }
                }.focusTarget(),
        )
        Box(
            Modifier
                .focusRequester(forwardRequester)
                .onFocusChanged {
                    if (it.isFocused && !isRequesting) {
                        focusManager.clearFocus(force = true)

                        val component = info.container.lastElementChild
                        if (component != null) {
                            requestFocus(component)
                        } else {
                            moveBackward()
                        }
                    }
                }.focusTarget(),
        )
    }
}

/**
 * A utility class for updating a component's view in response to state changes
 */
class Updater<T : Element>(
    private val component: T,
    update: (T) -> Unit,
) {
    private var isDisposed = false

    private val snapshotObserver =
        SnapshotStateObserver { command ->
            command()
        }

    private val scheduleUpdate = { _: T ->
        if (isDisposed.not()) {
            performUpdate()
        }
    }

    var update: (T) -> Unit = update
        set(value) {
            if (field != value) {
                field = value
                performUpdate()
            }
        }

    private fun performUpdate() {
        snapshotObserver.observeReads(component, scheduleUpdate) {
            update(component)
        }
    }

    init {
        snapshotObserver.start()
        performUpdate()
    }

    fun dispose() {
        snapshotObserver.stop()
        snapshotObserver.clear()
        isDisposed = true
    }
}

/**
 * Composable for displaying a URL in an HtmlView
 */
@Composable
fun HtmlViewUrl(
    url: String,
    modifier: Modifier = Modifier,
    headers: Map<String, String> = emptyMap(),
    navigator: HtmlViewNavigator = rememberHtmlViewNavigator(),
) {
    val state = rememberHtmlViewState()

    LaunchedEffect(url, headers) {
        state.content = HtmlContent.Url(url, headers)
    }

    HtmlView(
        state = state,
        modifier = modifier,
        navigator = navigator,
        onCreated = {},
        onDispose = {},
    )
}

/**
 * Composable for displaying HTML content in an HtmlView
 */
@Composable
fun HtmlViewContent(
    htmlContent: String,
    modifier: Modifier = Modifier,
    baseUrl: String? = null,
    navigator: HtmlViewNavigator = rememberHtmlViewNavigator(),
) {
    val state = rememberHtmlViewState()

    LaunchedEffect(htmlContent, baseUrl) {
        state.content = HtmlContent.Data(htmlContent, baseUrl)
    }

    HtmlView(
        state = state,
        modifier = modifier,
        navigator = navigator,
        onCreated = {},
        onDispose = {},
    )
}

/**
 * Create and remember an HtmlViewState instance
 */
@Composable
fun rememberHtmlViewState(): HtmlViewState = remember { HtmlViewState() }

// Container for HTML elements
val LocalLayerContainer =
    staticCompositionLocalOf {
        document.body!!
    }
