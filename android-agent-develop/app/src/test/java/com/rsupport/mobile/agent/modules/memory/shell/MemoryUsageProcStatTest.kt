package com.rsupport.mobile.agent.modules.memory.shell

import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MemoryUsageProcStatTest {
    private val statString = """
        32087 (rt.mobile.agent) S 5493 5493 0 0 -1 1077952832 42591 0 0 0 224 38 0 0 10 -10 59 0 78501068 7008333824 45917 18446744073709551615 1 1 0 0 0 0 4612 1 1073775864 0 0 0 17 3 0 0 0 0 0 0 0 0 0 0 0 0 0
    """.trimIndent()

    // proc/pid/stat 정상적인 데이터를 로드 했을때 메모리 정보를 읽어오는지 확인한다.
    @Test
    fun readMemoryTest() {
        val pkgName = "test.pkg"
        val memoryUsageShell = MemoryUsageProcStat(pkgName, statString)
        MatcherAssert.assertThat("메모리를 사용량을 로드하지 못해서 실패", memoryUsageShell.usageByte, Matchers.`is`(188076032L))
    }
}