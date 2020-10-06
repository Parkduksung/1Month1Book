package com.rsupport.mobile.agent.modules.sysinfo.cpu.rsperm

import com.rsupport.mobile.agent.extension.guard
import com.rsupport.mobile.agent.modules.memory.dumpsys.DumpsysReader
import com.rsupport.util.log.RLog
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import utils.bindRperm


class CpuUsageRspermFactoryTest {


    // Rsperm 이 설치되어 있을때 cpu 정보를 정상 로드하는지 확인한다.
    @Test
    fun getCpuUsageWhenInstalledRspermTest() = runBlocking<Unit> {
        bindRperm().guard {
            RLog.w("rsperm 과 바인딩 되지 않아서 테스트 하지 않는다.")
            return@runBlocking
        }

        val usageContainer = CpuUsageRspermFactory(DumpsysReader.createCpuInfo()).get()
        MatcherAssert.assertThat("Rsperm 을 이용한 cpu 사용량 조회를 실패", usageContainer.empty, Matchers.`is`(false))
    }
}

