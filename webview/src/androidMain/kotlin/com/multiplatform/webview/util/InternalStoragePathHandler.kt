package com.multiplatform.webview.util

import android.util.Log
import android.webkit.WebResourceResponse
import androidx.webkit.WebViewAssetLoader
import java.io.File
import java.io.FileInputStream

class InternalStoragePathHandler : WebViewAssetLoader.PathHandler {
    override fun handle(path: String): WebResourceResponse? {
        Log.d("InternalStorageHandler", "Intercepted: $path")
        val file = File(path.removePrefix("/"))
        if (!file.exists() || !file.isFile) return null

        val mimeType =
            when {
                path.endsWith(".html") -> "text/html"
                path.endsWith(".js") -> "application/javascript"
                path.endsWith(".css") -> "text/css"
                path.endsWith(".json") -> "application/json"
                path.endsWith(".png") -> "image/png"
                path.endsWith(".jpg") || path.endsWith(".jpeg") -> "image/jpeg"
                path.endsWith(".svg") -> "image/svg+xml"
                path.endsWith(".webp") -> "image/webp"
                path.endsWith(".ico") -> "image/x-icon"
                path.endsWith(".woff") -> "font/woff"
                path.endsWith(".woff2") -> "font/woff2"
                path.endsWith(".ttf") -> "font/ttf"
                path.endsWith(".mp4") -> "video/mp4"
                path.endsWith(".webm") -> "video/webm"
                path.endsWith(".ogg") -> "video/ogg"
                path.endsWith(".mp3") -> "audio/mpeg"
                path.endsWith(".wav") -> "audio/wav"
                path.endsWith(".wasm") -> "application/wasm"
                path.endsWith(".pdf") -> "application/pdf"
                path.endsWith(".zip") -> "application/zip"
                path.endsWith(".csv") -> "text/csv"
                else -> "application/octet-stream"
            }

        return WebResourceResponse(mimeType, "utf-8", FileInputStream(file))
    }
}
