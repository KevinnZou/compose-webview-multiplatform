package com.multiplatform.webview.util

fun Pair<Number, Number>?.isZero(): Boolean = this == null || (first == 0 && second == 0)

fun Pair<Number, Number>?.notZero(): Boolean = !isZero()
