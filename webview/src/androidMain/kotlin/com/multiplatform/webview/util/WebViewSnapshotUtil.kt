package com.multiplatform.webview.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.webkit.WebView
import androidx.compose.ui.geometry.Rect

internal fun WebView.takeSnapshot(rect: Rect?): Bitmap? {
    // Create a Rect object representing the area to capture.
    // If the provided rect is null, default to the full dimensions of the WebView.
    val snapshotRect = Rect(
        left = rect?.left ?: 0f,
        top = rect?.top ?: 0f,
        right = rect?.right ?: width.toFloat(),
        bottom = rect?.bottom ?: height.toFloat()
    )

    // Calculate the scale factor and dimensions
    val (scaledWidth, scaledHeight, scaleFactor) = WebViewSnapshotUtil.calculateScaleFactorAndDimensions(
        snapshotRect.width.toInt(),
        snapshotRect.height.toInt()
    )

    // Create a bitmap with the target dimensions
    val bm = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)

    // Create a canvas to draw the WebView content
    val scaledCanvas = Canvas(bm)

    // Apply the scaling transformation to the canvas and adjust for scroll position
    val matrix = Matrix().apply {
        setScale(scaleFactor, scaleFactor)
        postTranslate(
            -snapshotRect.left * scaleFactor - scrollX * scaleFactor,
            -snapshotRect.top * scaleFactor - scrollY * scaleFactor
        )
    }
    scaledCanvas.setMatrix(matrix)

    // Draw the WebView onto the scaled canvas
    draw(scaledCanvas)
    return bm
}

object WebViewSnapshotUtil {

    fun calculateScaleFactorAndDimensions(
        width: Int,
        height: Int
    ): Triple<Int, Int, Float> {
        val targetWidth = minOf(MAX_SNAPSHOT_WIDTH, width)
        val targetHeight = minOf(MAX_SNAPSHOT_HEIGHT, height)

        // Determine the aspect ratio preserving scale factor
        val scaleFactor = minOf(
            targetWidth.toFloat() / width,
            targetHeight.toFloat() / height
        )

        // Calculate the scaled dimensions
        val scaledWidth = (width * scaleFactor).toInt()
        val scaledHeight = (height * scaleFactor).toInt()

        return Triple(scaledWidth, scaledHeight, scaleFactor)
    }

    private const val MAX_SNAPSHOT_WIDTH = 2500
    private const val MAX_SNAPSHOT_HEIGHT = 2500
}