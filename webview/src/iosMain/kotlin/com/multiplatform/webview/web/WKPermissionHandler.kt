package com.multiplatform.webview.web

import platform.WebKit.WKFrameInfo
import platform.WebKit.WKMediaCaptureType
import platform.WebKit.WKPermissionDecision
import platform.WebKit.WKSecurityOrigin
import platform.WebKit.WKUIDelegateProtocol
import platform.WebKit.WKWebView
import platform.darwin.NSObject

class WKPermissionHandler(private val handler: PermissionHandler) : NSObject(), WKUIDelegateProtocol {
    override fun webView(
        webView: WKWebView,
        requestMediaCapturePermissionForOrigin: WKSecurityOrigin,
        initiatedByFrame: WKFrameInfo,
        type: WKMediaCaptureType,
        decisionHandler: (WKPermissionDecision) -> Unit
    ) {
        val request = PermissionRequest(
            requestMediaCapturePermissionForOrigin.string(),
            type.toPermissions()
        )
        val decision = handler(request).toDecision()
        decisionHandler(decision)
    }
}

private fun WKSecurityOrigin.string() = "${protocol}://${host}:${port}"

private fun PermissionRequestResponse.toDecision() = when(this) {
    PermissionRequestResponse.GRANT -> WKPermissionDecision.WKPermissionDecisionGrant
    PermissionRequestResponse.DENY -> WKPermissionDecision.WKPermissionDecisionDeny
}

private fun WKMediaCaptureType.toPermissions() = buildList {
    when(this@toPermissions) {
        WKMediaCaptureType.WKMediaCaptureTypeCamera -> add(PermissionRequest.Permission.VIDEO)
        WKMediaCaptureType.WKMediaCaptureTypeCameraAndMicrophone ->
            addAll(listOf(PermissionRequest.Permission.VIDEO, PermissionRequest.Permission.AUDIO))
        WKMediaCaptureType.WKMediaCaptureTypeMicrophone -> add(PermissionRequest.Permission.AUDIO)
        else -> error("Unknown capture type: $this")
    }
}