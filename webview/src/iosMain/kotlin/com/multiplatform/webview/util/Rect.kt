package com.multiplatform.webview.util

import androidx.compose.ui.geometry.Rect
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGFloat
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.UIKit.UIScreen

/**
 * Created By Murod 2024/07/20
 */

/**
 * Converts a Kotlin `Rect` to a native `CGRect` for iOS.
 *
 * This function transforms a `Rect` (using pixels) into a `CGRect` (using points) for use in iOS
 * development. It adjusts the rectangle’s coordinates and dimensions accordingly.
 *
 * @return A `CValue<CGRect>` representing the rectangle in iOS points.
 */
@OptIn(ExperimentalForeignApi::class)
fun Rect.toCGRect(): CValue<CGRect> {
    return CGRectMake(
        x = left.toPints(),// X coordinate in points
        y = top.toPints(), // Y coordinate in points
        width = width.toPints(), // Width in points
        height = height.toPints() // Height in points
    )
}

/**
 * On iOS, UI elements are measured in points, which are resolution-independent units.
 * The actual number of pixels per point depends on the device's screen scale factor.
 *
 * Function to convert pixels to points
 */
fun Float.toPints(): CGFloat {
    return this / UIScreen.mainScreen.scale
}