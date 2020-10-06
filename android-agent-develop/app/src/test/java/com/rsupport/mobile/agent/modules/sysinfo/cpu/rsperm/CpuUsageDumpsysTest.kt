package com.rsupport.mobile.agent.modules.sysinfo.cpu.rsperm

import com.rsupport.mobile.agent.modules.memory.LineSplitter
import com.rsupport.mobile.agent.modules.memory.dumpsys.CPUDumpsysReaderFromRsperm
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test

class CpuUsageDumpsysTest {
    private val dumpsysString = """
            CPU usage from 822315ms to 817022ms ago (2020-04-23 11:10:35.924 to 2020-04-23 11:10:41.217):
              25% 1334/system_server: 17% user + 7.7% kernel / faults: 1359 minor
              16% 9040/com.android.providers.calendar: 11% user + 5.6% kernel / faults: 1418 minor
              11% 697/surfaceflinger: 4.3% user + 6.8% kernel
              10% 2327/com.android.systemui:InfinityWallpaper: 6.6% user + 3.9% kernel
              8.7% 11347/adbd: 0.9% user + 7.7% kernel / faults: 386 minor
              8.3% 9010/com.google.android.syncadapters.calendar: 7.5% user + 0.7% kernel / faults: 1709 minor
              3.1% 13228/com.google.android.googlequicksearchbox:search: 1.9% user + 1.2% kernel / faults: 3553 minor 2297 major
              5.4% 550/logd: 2.2% user + 3.2% kernel / faults: 31 minor 2 major
              3.4% 2368/com.google.android.gms.persistent: 2.4% user + 0.9% kernel / faults: 870 minor
              3.4% 8732/com.google.android.apps.docs: 1.5% user + 1.8% kernel / faults: 319 minor
              3.2% 24929/kworker/u16:12: 0% user + 3.2% kernel
              2.8% 3446/kworker/u16:15: 0% user + 2.8% kernel
              2% 29603/kworker/u16:13: 0% user + 2% kernel
              1.3% 8397/com.google.android.gm: 0.7% user + 0.5% kernel / faults: 14 minor
              1.1% 88/smem_native_rpm: 0% user + 1.1% kernel
              1.1% 1070/wificond: 0% user + 1.1% kernel
              0.9% 25208/logcat: 0.1% user + 0.7% kernel
              0.7% 367/kworker/u17:0: 0% user + 0.7% kernel
              0.7% 414/cfinteractive: 0% user + 0.7% kernel
              0.7% 3080/iod: 0.1% user + 0.5% kernel
              0.5% 33/ksoftirqd/3: 0% user + 0.5% kernel
              0.5% 832/kworker/u17:1: 0% user + 0.5% kernel
              0.5% 2469/wpa_supplicant: 0% user + 0.5% kernel
              0.5% 2711/com.google.android.gms: 0.5% user + 0% kernel / faults: 164 minor 1 major
              0.1% 18267/com.samsung.android.app.spage: 0.1% user + 0% kernel / faults: 222 minor 14 major
              0.3% 7/rcu_preempt: 0% user + 0.3% kernel
              0.3% 28/rcuop/2: 0% user + 0.3% kernel
              0% 1155/ipacm: 0% user + 0% kernel
              0.3% 25786/kworker/5:2: 0% user + 0.3% kernel
              0.1% 8/rcu_sched: 0% user + 0.1% kernel
              0.1% 10/rcuop/0: 0% user + 0.1% kernel
              0.1% 20/rcuop/1: 0% user + 0.1% kernel
              0.1% 25/ksoftirqd/2: 0% user + 0.1% kernel
              0% 29/rcuos/2: 0% user + 0% kernel
              0% 45/rcuos/4: 0% user + 0% kernel
              0% 52/rcuop/5: 0% user + 0% kernel
              0.1% 551/servicemanager: 0% user + 0.1% kernel
              0.1% 641/android.hardware.wifi@1.0-service: 0% user + 0.1% kernel
              0.1% 694/lmkd: 0% user + 0.1% kernel
              0% 930/jbd2/dm-0-8: 0% user + 0% kernel
              0% 1082/smdexe: 0% user + 0% kernel / faults: 1 minor
              0% 1324/rild: 0% user + 0% kernel / faults: 6 minor 1 major
              0.1% 2192/com.android.systemui: 0% user + 0.1% kernel / faults: 3 minor
              0% 2534/com.android.phone: 0% user + 0% kernel / faults: 10 minor
              0.1% 2693/kworker/3:0: 0% user + 0.1% kernel
              0% 7248/com.samsung.android.calendar: 0% user + 0% kernel / faults: 7 minor
              0% 7435/com.google.android.apps.photos: 0% user + 0% kernel / faults: 8 minor
              0% 9320/com.sec.android.app.clockpackage: 0% user + 0% kernel / faults: 8 minor
              0% 12201/com.android.bluetooth: 0% user + 0% kernel / faults: 1 minor
              0% 13499/com.google.android.googlequicksearchbox:interactor: 0% user + 0% kernel / faults: 115 minor 16 major
              0% 24424/kworker/2:3: 0% user + 0% kernel
             +0% 9470/kworker/1:0: 0% user + 0% kernel
            15% TOTAL: 7.7% user + 6.4% kernel + 0.3% iowait + 0.5% irq + 0.3% softirq
    """.trimIndent()

    // cpu 사용량 text line 이 정상일대 cpu 정보가 사용 가능한지 확인한다. ( "/$pkgname:" 형식 )
    @Test
    fun cpuUsageAvailableTest() = runBlocking<Unit> {
        val textLine = "25% 1334/system_server: 17% user + 7.7% kernel / faults: 1359 minor"
        val cpuUsageDumpsys = CpuUsageDumpsys(textLine)
        MatcherAssert.assertThat("정상 데이터인데 사용할 수 없어서 실패", cpuUsageDumpsys.isAvailable, Matchers.`is`(true))
    }

    // cpu 사용량 text line 이 정상일대 cpu 사용량이 로드되는지 확인한다. ( "/$pkgname:" 형식 )
    @Test
    fun cpuUsageTest() = runBlocking<Unit> {
        val textLine = "25% 1334/system_server: 17% user + 7.7% kernel / faults: 1359 minor"
        val cpuUsageDumpsys = CpuUsageDumpsys(textLine)
        MatcherAssert.assertThat("cpu 사용량을 제대로 읽지 못해서 실패", cpuUsageDumpsys.percent, Matchers.`is`(25))
    }

    // cpu 사용량 text line 이 정상일대 key 가 packageName 으로 로드되는지 확인한다. ( "/$pkgname:" 형식 )
    @Test
    fun cpuUsagePkgNameTest() = runBlocking<Unit> {
        val textLine = "25% 1334/system_server: 17% user + 7.7% kernel / faults: 1359 minor"
        val cpuUsageDumpsys = CpuUsageDumpsys(textLine)
        MatcherAssert.assertThat("cpu 사용량을 제대로 읽지 못해서 실패", cpuUsageDumpsys.keyObject.name, Matchers.`is`("system_server"))
    }


    // cpu 사용량 text line 이 정상일대 cpu 정보가 사용 가능한지 확인한다. ( "/$pkgname/$id:$id" 형식 )
    @Test
    fun multiCpuUsageAvailableTest() = runBlocking<Unit> {
        val textLine = "3.2% 24929/kworker/u16:12: 0% user + 3.2% kernel"
        val cpuUsageDumpsys = CpuUsageDumpsys(textLine)
        MatcherAssert.assertThat("정상 데이터인데 사용할 수 없어서 실패", cpuUsageDumpsys.isAvailable, Matchers.`is`(true))
    }

    // cpu 사용량 text line 이 정상일대 cpu 사용량이 로드되는지 확인한다. ( "/$pkgname/$id:$id" 형식 )
    @Test
    fun multiCpuUsageTest() = runBlocking<Unit> {
        val textLine = "3.2% 24929/kworker/u16:12: 0% user + 3.2% kernel"
        val cpuUsageDumpsys = CpuUsageDumpsys(textLine)
        MatcherAssert.assertThat("cpu 사용량을 제대로 읽지 못해서 실패", cpuUsageDumpsys.percent, Matchers.`is`(3))
    }

    // cpu 사용량 text line 이 정상일대 key 가 packageName 으로 로드되는지 확인한다. ( "/$pkgname/$id:$id" 형식 )
    @Test
    fun multiCpuUsagePkgNameTest() = runBlocking<Unit> {
        val textLine = "3.2% 24929/kworker/u16:12: 0% user + 3.2% kernel"
        val cpuUsageDumpsys = CpuUsageDumpsys(textLine)
        MatcherAssert.assertThat("cpu 사용량을 제대로 읽지 못해서 실패", cpuUsageDumpsys.keyObject.name, Matchers.`is`("kworker"))
    }

    // cpu 사용량이 0.5 이상일때 반올림 되는지 확인한다.
    @Test
    fun cpuUsageRoundTest() = runBlocking<Unit> {
        val textLine = "3.5% 24929/kworker/u16:12: 0% user + 3.2% kernel"
        val cpuUsageDumpsys = CpuUsageDumpsys(textLine)
        MatcherAssert.assertThat("cpu 사용량이 반올림 안되서 실패", cpuUsageDumpsys.percent, Matchers.`is`(4))
    }

    // cpu 사용량이 0.5 미만일때 버려지는지 확인한다.
    @Test
    fun cpuUsageRound2Test() = runBlocking<Unit> {
        val textLine = "3.4% 24929/kworker/u16:12: 0% user + 3.2% kernel"
        val cpuUsageDumpsys = CpuUsageDumpsys(textLine)
        MatcherAssert.assertThat("cpu 이 0.4 이하인데 버려지지 않아서 실패", cpuUsageDumpsys.percent, Matchers.`is`(3))
    }

    @Test
    fun packageNameTest() {
        LineSplitter(dumpsysString).getLines().forEach { textLine ->
            val cpuUsageDumpsys = CpuUsageDumpsys(textLine)
            val pkgName = cpuUsageDumpsys.keyObject.name
            MatcherAssert.assertThat("packageName 을 찾지 못해서 실패", textLine, Matchers.containsString(pkgName))
        }
    }
}