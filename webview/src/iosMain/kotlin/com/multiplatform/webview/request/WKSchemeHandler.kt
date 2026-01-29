package com.multiplatform.webview.request

import com.multiplatform.webview.util.KLogger
import com.multiplatform.webview.web.WebViewNavigator
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.HTTPMethod
import platform.Foundation.NSData
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSURL
import platform.Foundation.allHTTPHeaderFields
import platform.Foundation.create
import platform.WebKit.WKURLSchemeHandlerProtocol
import platform.WebKit.WKURLSchemeTaskProtocol
import platform.WebKit.WKWebView
import platform.darwin.NSObject

/**
 * WKURLSchemeHandler implementation for custom URL schemes.
 * This allows intercepting requests with custom schemes (e.g., "app://", "local://")
 * and providing custom responses.
 *
 * Note: WKURLSchemeHandler methods are called on the main thread by WebKit,
 * so the activeTasks map access is thread-safe in this context.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class WKSchemeHandler(
    private val navigator: WebViewNavigator,
) : NSObject(),
    WKURLSchemeHandlerProtocol {
    private val activeTasks = mutableMapOf<Int, Boolean>()

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        startURLSchemeTask: WKURLSchemeTaskProtocol,
    ) {
        val taskId = startURLSchemeTask.hashCode()
        activeTasks[taskId] = true

        val request = startURLSchemeTask.request
        val url = request.URL?.absoluteString ?: ""

        KLogger.info { "WKSchemeHandler: Intercepting request for $url" }

        // Build WebRequest
        val headerMap = mutableMapOf<String, String>()
        request.allHTTPHeaderFields?.forEach {
            headerMap[it.key.toString()] = it.value.toString()
        }

        // WKURLSchemeTaskProtocol does not expose frame info directly.
        // Assume main frame for custom scheme requests as a reasonable default.
        val isForMainFrame = true

        val webRequest =
            WebRequest(
                url = url,
                headers = headerMap,
                isForMainFrame = isForMainFrame,
                isRedirect = false,
                method = request.HTTPMethod ?: "GET",
            )

        // Check if we have an interceptor
        val interceptor = navigator.requestInterceptor
        if (interceptor == null) {
            KLogger.w { "WKSchemeHandler: No request interceptor set, failing request" }
            failTask(startURLSchemeTask, "No request interceptor configured")
            activeTasks.remove(taskId)
            return
        }

        try {
            // Call the interceptor
            val result = interceptor.onInterceptUrlRequest(webRequest, navigator)

            // Check if task was cancelled
            if (activeTasks[taskId] != true) {
                KLogger.info { "WKSchemeHandler: Task was cancelled" }
                failTask(startURLSchemeTask, "Task was cancelled")
                activeTasks.remove(taskId)
                return
            }

            when (result) {
                is WebRequestInterceptResult.Respond -> {
                    respondWithData(startURLSchemeTask, result, url)
                }
                is WebRequestInterceptResult.Reject -> {
                    failTask(startURLSchemeTask, "Request rejected by interceptor")
                }
                else -> {
                    // For Allow and Modify, we can't actually make the request
                    // because this is a custom scheme. Return an error.
                    failTask(startURLSchemeTask, "Custom scheme requires Respond result")
                }
            }
        } catch (e: Exception) {
            KLogger.e { "WKSchemeHandler: Exception in request interceptor: ${e.message}" }
            failTask(startURLSchemeTask, "Request interceptor threw an exception: ${e.message}")
        } finally {
            activeTasks.remove(taskId)
        }
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        stopURLSchemeTask: WKURLSchemeTaskProtocol,
    ) {
        val taskId = stopURLSchemeTask.hashCode()
        activeTasks[taskId] = false
        KLogger.info { "WKSchemeHandler: Task stopped" }
    }

    private fun respondWithData(
        task: WKURLSchemeTaskProtocol,
        result: WebRequestInterceptResult.Respond,
        url: String,
    ) {
        try {
            // Validate URL
            val nsUrl = NSURL.URLWithString(url)
            if (nsUrl == null) {
                val message = "WKSchemeHandler: Invalid URL: $url"
                KLogger.e { message }
                failTask(task, message)
                return
            }

            // Build response headers
            // Add custom headers first, then set Content-Type from mimeType to ensure
            // mimeType takes precedence over any Content-Type in headers
            val headerFields = mutableMapOf<Any?, Any?>()
            result.headers.forEach { (key, value) ->
                // Skip Content-Type from headers - we use result.mimeType instead
                if (!key.equals("Content-Type", ignoreCase = true)) {
                    headerFields[key] = value
                }
            }
            headerFields["Content-Type"] = result.mimeType
            headerFields["Content-Length"] = result.data.size.toString()

            // Create HTTP response
            val response =
                NSHTTPURLResponse(
                    uRL = nsUrl,
                    statusCode = result.statusCode.toLong(),
                    HTTPVersion = "HTTP/1.1",
                    headerFields = headerFields,
                )

            if (response == null) {
                failTask(task, "Failed to create HTTP response")
                return
            }

            // Send response
            task.didReceiveResponse(response)

            // Send data
            if (result.data.isNotEmpty()) {
                result.data.usePinned { pinned ->
                    val nsData =
                        NSData.create(
                            bytes = pinned.addressOf(0),
                            length = result.data.size.toULong(),
                        )
                    task.didReceiveData(nsData)
                }
            }

            // Finish
            task.didFinish()

            KLogger.info { "WKSchemeHandler: Successfully responded with ${result.data.size} bytes" }
        } catch (e: Exception) {
            KLogger.e { "WKSchemeHandler: Error responding: ${e.message}" }
            failTask(task, e.message ?: "Unknown error")
        }
    }

    private fun failTask(
        task: WKURLSchemeTaskProtocol,
        message: String,
    ) {
        try {
            val error =
                platform.Foundation.NSError.errorWithDomain(
                    domain = "WKSchemeHandler",
                    code = -1,
                    userInfo = mapOf("NSLocalizedDescriptionKey" to message),
                )
            task.didFailWithError(error)
        } catch (e: Exception) {
            KLogger.e { "WKSchemeHandler: Error failing task: ${e.message}" }
        }
    }
}
