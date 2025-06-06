package com.multiplatform.webview.web

/**
 * Defines the source location for loading an HTML file into the WebView.
 *
 * This enum specifies whether the HTML file should be loaded from the platform-specific
 * resources folder or from the Compose Multiplatform "composeResources/files" directory.
 *
 * - [ASSET_RESOURCES]: Loads the HTML file from the platform's `resources/assets` folder.
 *
 * - [COMPOSE_RESOURCE_FILES]: Loads the HTML file from the `composeResources/files`
 *   directory. This is the officially recommended approach for accessing file resources
 *   in a platform-agnostic way using `Res.readBytes()`.
 */
enum class WebViewFileReadType {
    ASSET_RESOURCES,
    COMPOSE_RESOURCE_FILES,
}
