package com.rsupport.mobile.agent.modules.memory

import com.rsupport.mobile.agent.modules.memory.dumpsys.MemoryUsageDumpsys
import com.rsupport.mobile.agent.modules.memory.knox.MemoryUsageKnox
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UsageContainerTest {

    @Test
    fun emptyMemoryUsageTest() = runBlocking<Unit> {
        val memoryUsageContainer = UsageContainer(emptyList())

        val memoryUsage = memoryUsageContainer.find(KeyObject("1"))

        MatcherAssert.assertThat("데이터가 없는데 결과가 잇어서 실패", memoryUsage, Matchers.nullValue())
    }

    // 1개 pid 를 입력햇을때 확인한다.
    @Test
    fun memoryUsageFindByPidTest() = runBlocking<Unit> {
        val memoryUsageContainer = UsageContainer(listOf(
                MemoryUsageDumpsys("139,067K: system (pid 1597)")
        ))
        val memoryUsage = memoryUsageContainer.find(KeyObject("system"))
        MatcherAssert.assertThat("데이터가 없어서 실패", memoryUsage?.keyObject?.name, Matchers.`is`("system"))
    }

    // 여러개를 pid 중 첫번째만 PID 를 찾을때 결과를 확인한다.
    @Test
    fun memoryUsageListFirstFindByPidTest() = runBlocking<Unit> {
        val memoryUsageContainer = UsageContainer(listOf(
                MemoryUsageDumpsys("139,067K: system (pid 1597)"),
                MemoryUsageDumpsys("139,067K: system1 (pid 1598)"),
                MemoryUsageDumpsys("139,067K: system2 (pid 1599)"),
                MemoryUsageDumpsys("139,067K: system3 (pid 1560)")
        ))
        val memoryUsage = memoryUsageContainer.find(KeyObject("system"))
        MatcherAssert.assertThat("데이터가 없어서 실패", memoryUsage?.keyObject?.name, Matchers.`is`("system"))
    }

    // 여러개를 pid 중 마지막 PID 를 찾을때 결과를 확인한다.
    @Test
    fun memoryUsageListFindByPidTest() = runBlocking<Unit> {
        val memoryUsageContainer = UsageContainer(listOf(
                MemoryUsageDumpsys("139,067K: system (pid 1597)"),
                MemoryUsageDumpsys("139,067K: system1 (pid 1598)"),
                MemoryUsageDumpsys("139,067K: system2 (pid 1599)"),
                MemoryUsageDumpsys("139,067K: system3 (pid 1560)")
        ))
        val memoryUsage = memoryUsageContainer.find(KeyObject("system3"))
        MatcherAssert.assertThat("데이터가 없어서 실패", memoryUsage?.keyObject?.name, Matchers.`is`("system3"))
    }

    // 여러개를 pid 중 같은 pid 가 있을때 잘 찾는지 결과를 확인한다.
    @Test
    fun memoryUsageDuplicateListFindByPidTest() = runBlocking<Unit> {
        val memoryUsageContainer = UsageContainer(listOf(
                MemoryUsageDumpsys("139,067K: system (pid 1597)"),
                MemoryUsageDumpsys("139,067K: system1 (pid 1598)"),
                MemoryUsageDumpsys("139,067K: system2 (pid 1599)"),
                MemoryUsageDumpsys("139,067K: system3 (pid 1560)"),
                MemoryUsageDumpsys("139,067K: system4 (pid 1597)")
        ))
        val memoryUsage = memoryUsageContainer.find(KeyObject("system"))
        MatcherAssert.assertThat("데이터가 없어서 실패", memoryUsage?.keyObject?.name, Matchers.`is`("system"))
    }

    // 1개 pakage 를 입력햇을때 확인한다.
    @Test
    fun memoryUsageFindByPkgTest() = runBlocking<Unit> {
        val memoryUsageContainer = UsageContainer(listOf(
                MemoryUsageKnox("system", 1234)
        ))
        val memoryUsage = memoryUsageContainer.find(KeyObject("system"))
        MatcherAssert.assertThat("데이터가 없어서 실패", memoryUsage?.keyObject?.name, Matchers.`is`("system"))
    }

    // 2개이상의 pkgName 이 있을때 확인한다.
    @Test
    fun memoryUsageListFindByPkgTest() = runBlocking<Unit> {
        val memoryUsageContainer = UsageContainer(listOf(
                MemoryUsageKnox("system", 1234),
                MemoryUsageKnox("system1", 1235),
                MemoryUsageKnox("system2", 1235),
                MemoryUsageKnox("system3", 1235)
        ))
        val memoryUsage = memoryUsageContainer.find(KeyObject("system"))
        MatcherAssert.assertThat("데이터가 없어서 실패", memoryUsage?.keyObject?.name, Matchers.`is`("system"))
    }
}

