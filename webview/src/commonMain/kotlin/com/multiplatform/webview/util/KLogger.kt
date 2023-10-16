package com.multiplatform.webview.util

import co.touchlab.kermit.DefaultFormatter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import co.touchlab.kermit.platformLogWriter

/**
 * Created By Kevin Zou On 2023/10/16
 */
internal object KLogger : Logger(
    config = loggerConfigInit(
        platformLogWriter(DefaultFormatter),
        minSeverity = Severity.Debug
    ),
    tag = "ComposeWebView"
)