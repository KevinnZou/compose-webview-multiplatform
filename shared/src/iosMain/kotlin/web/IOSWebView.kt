package web

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import platform.Foundation.HTTPBody
import platform.Foundation.HTTPMethod
import platform.Foundation.NSData
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.create
import platform.WebKit.WKWebView

/**
 * Created By Kevin Zou On 2023/9/5
 */
class IOSWebView(private val wkWebView: WKWebView) : IWebView {

    override fun canGoBack() = wkWebView.canGoBack

    override fun canGoForward() = wkWebView.canGoForward

    override fun loadUrl(url: String, additionalHttpHeaders: Map<String, String>) {
        wkWebView.loadRequest(
            request = NSURLRequest(
                uRL = NSURL(string = url),
            )
        )
    }

    override fun loadHtml(
        html: String?,
        baseUrl: String?,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?
    ) {
        if (html == null || baseUrl == null) {
            return
        }
        wkWebView.loadHTMLString(
            string = html,
            baseURL = NSURL(string = baseUrl),
        )
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override fun postUrl(url: String, postData: ByteArray) {
        val request = NSMutableURLRequest(
            uRL = NSURL(string = url),
        )
        request.apply {
            HTTPMethod = "POST"
            HTTPBody = memScoped {
                NSData.create(bytes = allocArrayOf(postData), length = postData.size.toULong())
            }
        }
        wkWebView.loadRequest(request = request)
    }

    override fun goBack() {
        wkWebView.goBack()
    }

    override fun goForward() {
        wkWebView.goForward()
    }

    override fun reload() {
        wkWebView.reload()
    }

    override fun stopLoading() {
        wkWebView.stopLoading()
    }

}