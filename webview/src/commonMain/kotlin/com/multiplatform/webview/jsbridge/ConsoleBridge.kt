package com.multiplatform.webview.jsbridge

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Bridge for capturing platform WebView console logs and forwarding them to
 * the existing JS bridge as a JSON payload matching ConsoleLogMessage structure.
 */
class ConsoleBridge(
    /**
     * Callback invoked with a JSON string matching ConsoleLogMessage schema.
     */
    var onLog: ((String) -> Unit)? = null,
) {
    /**
     * Emit a console log event coming from the platform WebView.
     * This will be routed through the JS bridge using method "consoleLog".
     */
    fun emitFromPlatform(
        level: String,
        content: String,
        sourceId: String?,
        lineNumber: Int,
        timestamp: String,
    ) {
        val normalizedLevel = level.lowercase()
        val type =
            when (normalizedLevel) {
                "error" -> "error"
                "exception" -> "exception"
                else -> "normal"
            }
        val cause =
            when (normalizedLevel) {
                "warn", "warning" -> "warning"
                "error" -> "error"
                "debug" -> "debug"
                else -> "user_code"
            }
        val emoji =
            when (normalizedLevel) {
                "error" -> "❌"
                "warn", "warning" -> "⚠️"
                "debug" -> "🔍"
                "info" -> "ℹ️"
                else -> "📝"
            }
        val filePath = sourceId ?: ""
        val fileName = filePath.substringAfterLast('/').substringAfterLast('\\')

        val json: JsonObject =
            buildJsonObject {
                put("emoji", emoji)
                put("type", type)
                put("level", normalizedLevel)
                put("content", content)
                put("cause", cause)
                put("lineNumber", lineNumber)
                put("fileName", fileName)
                put("filePath", filePath)
                put("timestamp", timestamp)
            }

        val payload = Json.encodeToString(JsonObject.serializer(), json)

        // Emit directly to any listener without using WebViewJsBridge
        onLog?.invoke(payload)
    }
}
