package com.multiplatform.webview.web

sealed class CefException(override val message: String) : Exception(message) {
    data object NotInitialized : CefException("Cef was not initialized.")
    data object Disposed : CefException("Cef is disposed.")
    data object ApplicationRestartRequired : CefException("Application needs to restart.")

    data class Error(val exception: Throwable?) : CefException("Got error: ${exception?.message}")
}