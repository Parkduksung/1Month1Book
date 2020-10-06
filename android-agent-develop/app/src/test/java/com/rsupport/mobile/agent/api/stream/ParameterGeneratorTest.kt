package com.rsupport.mobile.agent.api.stream

import org.hamcrest.Matchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test

class ParameterGeneratorTest {

    @Test
    fun `데이터가 없으며 빈값을 반환한다`() {
        val parameterGenerator = ParameterGenerator()
        assert(parameterGenerator.generate(hashMapOf()).isEmpty())
    }

    @Test
    fun `Parameter 가 있으면 url encoding 으로 변환된다`() {
        val parameterGenerator = ParameterGenerator()
        val paramter = parameterGenerator.generate(hashMapOf("key" to "value"))
        assertEquals("key=value", paramter)
    }

    @Test
    fun `Parameter 가 한글이면 url encoding 으로 변환된다`() {
        val parameterGenerator = ParameterGenerator()
        val paramter = parameterGenerator.generate(hashMapOf("키" to "값"))
        assertEquals("%ED%82%A4=%EA%B0%92", paramter)
    }

    @Test
    fun `Parameter 여러개 있으면 & 로 Parameter 를 구분한다`() {
        val parameterGenerator = ParameterGenerator()
        val paramter = parameterGenerator.generate(
                hashMapOf("key" to "value", "key1" to "value1")
        )
        assertThat(paramter, Matchers.containsString("key=value"))
        assertThat(paramter, Matchers.containsString("&"))
        assertThat(paramter, Matchers.containsString("key1=value1"))
    }
}