package com.rsupport.util

class FixedSize(private val maxSize: Int, private val ratio: Float) {

    fun calculate(sourceWidth: Int, sourceHeight: Int): Pair<Int, Int> {
        val maxSource = sourceWidth.coerceAtLeast(sourceHeight)
        var fixedWidth = sourceWidth
        var fixedHeight = sourceHeight
        if (maxSource > maxSize) {
            fixedHeight = maxSize / 16 * 16
            fixedWidth = (fixedHeight / ratio).toInt() / 16 * 16
        }
        return Pair(fixedWidth, fixedHeight)
    }
}