package com.multiplatform.webview.util

import kotlin.io.path.createTempDirectory

val tempDirectory: java.io.File = createTempDirectory("webview-temp").toFile()

fun addTempDirectoryRemovalHook() {
    Runtime.getRuntime().addShutdownHook(
        Thread {
            println("Attempting to delete temp directory: ${tempDirectory.absolutePath}")
            val success = tempDirectory.deleteRecursively()
            if (success) {
                println("✅ Temp directory deleted successfully.")
            } else {
                println("❌ Failed to delete temp directory.")
            }
        },
    )
}
