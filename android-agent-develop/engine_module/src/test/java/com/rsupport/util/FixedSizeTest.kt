package com.rsupport.util

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Test

class FixedSizeTest {
    @Test
    fun shouldLessThanOrEqualMinSource() {
        val sourceWidth = 480
        val sourceHeight = 854

        val fixedSize = FixedSize(720, 1.6f)

        val (fixedWidth, fixedHeight) = fixedSize.calculate(sourceWidth, sourceHeight)
        assertThat(fixedWidth.coerceAtLeast(fixedHeight), Matchers.lessThanOrEqualTo(720))
    }
}

