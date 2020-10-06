package com.rsupport.mobile.agent.utils

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LogCollectorTest {

    // 1.지원하는 data 형식의 log collect 일때 push 성공해야한다.
    @Test
    fun supportedDataTypeTest() {
        val errorCode = 0
        val errorMessage = "errorMessage"
        val errorDescription = "errorDescription"

        val errorData = ErrorData(errorCode.toString(), errorMessage, errorDescription)

        val collectResult = Collector.push(errorData)
        MatcherAssert.assertThat("로그정보가 수집되지 않아서 실패", collectResult, Matchers.`is`(true))
    }

    // 2.지원하지 않는 data 형식의 log collect 일때 push 실패해야한다.
    @Test
    fun notSupportedDataTypeTest() {
        val collectResult = Collector.push("지원하지 않는 타입.")
        MatcherAssert.assertThat("로그정보가 수집되어서 실패", collectResult, Matchers.`is`(false))
    }
}