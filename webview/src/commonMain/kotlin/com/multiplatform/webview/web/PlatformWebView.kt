package com.multiplatform.webview.web

import androidx.compose.runtime.Composable

/**
 * Created By Kevin Zou On 2024/4/7
 */
expect class PlatformWebView

expect fun getPlatformWebView(context: PlatformContext): PlatformWebView?

expect class PlatformContext

@Composable
expect fun getPlatformContext(): PlatformContext
