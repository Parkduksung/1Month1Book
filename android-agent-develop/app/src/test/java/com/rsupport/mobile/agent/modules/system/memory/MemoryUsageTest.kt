package com.rsupport.mobile.agent.modules.system.memory

import com.rsupport.mobile.agent.modules.memory.LineSplitter
import com.rsupport.mobile.agent.modules.memory.MemoryUsage
import com.rsupport.mobile.agent.modules.memory.KeyObject
import com.rsupport.mobile.agent.modules.memory.dumpsys.DumpsysReader
import com.rsupport.mobile.agent.modules.memory.dumpsys.MemoryUsageDumpsys
import com.rsupport.mobile.agent.modules.memory.dumpsys.DumpsysMemoryUsageFactory
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MemoryUsageTest {
    private val dumpSysMemInfo = """
        Applications Memory Usage (in Kilobytes):
        Uptime: 8870072 Realtime: 8870072

        Total PSS by process:
            187,848K: surfaceflinger (pid 560)
            139,067K: system (pid 1597)
             35,464K: com.lge.launcher3 (pid 4044 / activities)
             19,368K: com.android.systemui (pid 2288)
              6,839K: mm-pp-dpps (pid 601)
              6,783K: qseeproxydaemon (pid 941)
              3,432K: com.lge.ime (pid 2283)
              3,077K: audiod (pid 948)
              2,392K: com.lge.signboard (pid 3248)
                964K: pm-service (pid 537)
                924K: ATFWD-daemon (pid 940)
                912K: fingerprintd (pid 946)
                800K: fidod (pid 952)
                788K: com.google.android.apps.maps (pid 16500)
                772K: pm-proxy (pid 580)
                714K: servicemanager (pid 559)
                664K: lgsecclkserver (pid 802)
                586K: lghashstorageserver (pid 839)
                582K: gbmd (pid 810)
                578K: prmd (pid 811)
                576K: ccmd (pid 557)
                525K: subsystem_ramdump (pid 579)
                356K: ipsecstarter (pid 820)
                240K: mcDriverDaemon (pid 758)
                  0K: com.google.android.googlequicksearchbox:search (pid 2942)
                  0K: com.google.android.gms (pid 2979)
                  0K: com.google.android.gms.persistent (pid 2553)
                  0K: com.android.defcontainer (pid 6342)
                  0K: com.skt.skaf.A000Z00040 (pid 3940)
                  0K: com.lge.clock (pid 16948)
                  0K: com.google.process.gapps (pid 11197)
                  0K: com.android.vending (pid 15311)
                  0K: com.google.android.gms.ui (pid 16653)
                  0K: com.rsupport.rs.activity.rsupport (pid 16294)
                  0K: com.rsupport.rs.activity.aig (pid 16255)
                  0K: com.google.android.apps.photos (pid 16459)
                  0K: com.lge.lockscreensettings (pid 16207)
                  0K: com.lge.email (pid 16168)
                  0K: com.lge.provider.lockscreensettings (pid 16015)
                  0K: com.lge.coneshortcut (pid 15821)
                  0K: com.lge.conerecent (pid 16631)
                  0K: com.lge.appbox.client (pid 16148)
                  0K: com.android.printspooler (pid 16050)
                  0K: android.process.acore (pid 3407)
                  0K: com.android.gallery3d (pid 16072)
                  0K: com.android.contacts (pid 16038)
                  0K: com.sec.android.app.sbrowser (pid 16335)
                  0K: com.rsupport.mobile.agent (pid 16532)
                  0K: com.wsandroid.suite.lge (pid 15127)
                  0K: com.skt.skaf.OA00199800 (pid 4372)
                  0K: com.lge.gametuner (pid 3297)
                  0K: android.process.media (pid 3625)
                  0K: com.google.android.youtube (pid 7707)
                  0K: com.lge.provider.signboard (pid 3372)
                  0K: com.lge.ime:com.rsupport.setting (pid 2377)
                  0K: com.android.bluetooth (pid 2773)
                  0K: com.lge.bnr (pid 2635)
                  0K: com.lge.music (pid 9425)
                  0K: com.android.calendar (pid 8341)
                  0K: com.skt.prod.phone (pid 6306)
                  0K: com.lge.sizechangable.musicwidget.widget (pid 4001)
                  0K: com.qualcomm.telephony (pid 2923)
                  0K: com.qualcomm.qcrilmsgtunnel (pid 2782)
                  0K: com.lge.springcleaning:AppCleanupService (pid 6188)
                  0K: com.lge.camera (pid 5820)
                  0K: com.lge.quicktools (pid 3984)
                  0K: com.android.incallui (pid 3619)
                  0K: com.sktelecom.smartcard.SmartcardService (pid 3239)
                  0K: com.skt.skaf.OA00412131 (pid 3221)
                  0K: com.android.phone (pid 3207)
                  0K: com.lge.mlt (pid 3186)
                  0K: com.lge.ims (pid 3171)
                  0K: .dataservices (pid 3161)
                  0K: com.lge.wifi.p2p (pid 3149)
                  0K: com.lge.maagent (pid 3136)
                  0K: com.android.server.telecom (pid 3112)
                  0K: com.lge.wfds.service.v3 (pid 3103)
                  0K: com.android.nfc (pid 3076)
                  0K: com.lge.findlostphone (pid 3065)
                  0K: com.lge.musiccontroller (pid 3050)
                  0K: com.lge.lgdmprovisioningclient (pid 3044)
                  0K: com.google.android.googlequicksearchbox:interactor (pid 3029)
                  0K: com.lge.atservice (pid 2880)
                  0K: com.google.android.ext.services (pid 2796)
                  0K: com.lge.myplace.engine (pid 2583)
                  0K: com.lge.systemserver (pid 2570)
                  0K: /init (pid 1)
                  0K: ueventd (pid 438)
                  0K: logd (pid 471)
                  0K: debuggerd (pid 477)
                  0K: debuggerd64 (pid 478)
                  0K: vold (pid 479)
                  0K: debuggerd:signaller (pid 481)
                  0K: debuggerd64:signaller (pid 482)
                  0K: healthd (pid 529)
                  0K: rmt_storage (pid 532)
                  0K: esepmdaemon (pid 536)
                  0K: qseecomd (pid 539)
                  0K: lmkd (pid 558)
                  0K: qseecomd (pid 575)
                  0K: sensors.qcom (pid 602)
                  0K: lgdrmserver (pid 803)
                  0K: tftp_server (pid 805)
                  0K: thermal-engine (pid 806)
                  0K: adsprpcd (pid 807)
                  0K: hvdcp_opti (pid 809)
                  0K: imswmsproxy (pid 821)
                  0K: libbroadcast_server (pid 822)
                  0K: atd (pid 830)
                  0K: zygote64 (pid 836)
                  0K: zygote (pid 837)
                  0K: audioserver (pid 842)
                  0K: cameraserver (pid 843)
                  0K: drmserver (pid 846)
                  0K: installd (pid 847)
                  0K: keystore (pid 850)
                  0K: media.codec (pid 851)
                  0K: mediadrmserver (pid 933)
                  0K: media.extractor (pid 934)
                  0K: mediaserver (pid 935)
                  0K: netd (pid 936)
                  0K: rild (pid 937)
                  0K: loc_launcher (pid 938)
                  0K: mm-qcamera-daemon (pid 942)
                  0K: time_daemon (pid 947)
                  0K: brcm-uim-sysfs (pid 950)
                  0K: rctd (pid 953)
                  0K: gatekeeperd (pid 955)
                  0K: ipacm-diag (pid 989)
                  0K: adbd (pid 991)
                  0K: ipacm (pid 1008)
                  0K: qti (pid 1064)
                  0K: netmgrd (pid 1075)
                  0K: lowi-server (pid 1367)
                  0K: xtwifi-inet-agent (pid 1368)
                  0K: xtwifi-client (pid 1369)
                  0K: slim_daemon (pid 1370)
                  0K: sdcard (pid 2301)
                  0K: sdcard (pid 2318)
                  0K: wpa_supplicant (pid 2498)
                  0K: triton (pid 2808)
                  0K: lgvrhid (pid 2825)
                  0K: perfd (pid 2851)
                  0K: bnrd (pid 5816)
                  0K: logcat (pid 12551)
                  0K: dsqn (pid 13688)
                  0K: sh (pid 16709)
                  0K: dumpsys (pid 17005)

        Total PSS by OOM adjustment:
            213,740K: Native
                187,848K: surfaceflinger (pid 560)
                  6,839K: mm-pp-dpps (pid 601)
                  6,783K: qseeproxydaemon (pid 941)
                  3,077K: audiod (pid 948)
                    964K: pm-service (pid 537)
                    924K: ATFWD-daemon (pid 940)
                    912K: fingerprintd (pid 946)
                    800K: fidod (pid 952)
                    772K: pm-proxy (pid 580)
                    714K: servicemanager (pid 559)
                    664K: lgsecclkserver (pid 802)
                    586K: lghashstorageserver (pid 839)
                    582K: gbmd (pid 810)
                    578K: prmd (pid 811)
                    576K: ccmd (pid 557)
                    525K: subsystem_ramdump (pid 579)
                    356K: ipsecstarter (pid 820)
                    240K: mcDriverDaemon (pid 758)
                      0K: /init (pid 1)
                      0K: ueventd (pid 438)
                      0K: logd (pid 471)
                      0K: debuggerd (pid 477)
                      0K: debuggerd64 (pid 478)
                      0K: vold (pid 479)
                      0K: debuggerd:signaller (pid 481)
                      0K: debuggerd64:signaller (pid 482)
                      0K: healthd (pid 529)
                      0K: rmt_storage (pid 532)
                      0K: esepmdaemon (pid 536)
                      0K: qseecomd (pid 539)
                      0K: lmkd (pid 558)
                      0K: qseecomd (pid 575)
                      0K: sensors.qcom (pid 602)
                      0K: lgdrmserver (pid 803)
                      0K: tftp_server (pid 805)
                      0K: thermal-engine (pid 806)
                      0K: adsprpcd (pid 807)
                      0K: hvdcp_opti (pid 809)
                      0K: imswmsproxy (pid 821)
                      0K: libbroadcast_server (pid 822)
                      0K: atd (pid 830)
                      0K: zygote64 (pid 836)
                      0K: zygote (pid 837)
                      0K: audioserver (pid 842)
                      0K: cameraserver (pid 843)
                      0K: drmserver (pid 846)
                      0K: installd (pid 847)
                      0K: keystore (pid 850)
                      0K: media.codec (pid 851)
                      0K: mediadrmserver (pid 933)
                      0K: media.extractor (pid 934)
                      0K: mediaserver (pid 935)
                      0K: netd (pid 936)
                      0K: rild (pid 937)
                      0K: loc_launcher (pid 938)
                      0K: mm-qcamera-daemon (pid 942)
                      0K: time_daemon (pid 947)
                      0K: brcm-uim-sysfs (pid 950)
                      0K: rctd (pid 953)
                      0K: gatekeeperd (pid 955)
                      0K: ipacm-diag (pid 989)
                      0K: adbd (pid 991)
                      0K: ipacm (pid 1008)
                      0K: qti (pid 1064)
                      0K: netmgrd (pid 1075)
                      0K: lowi-server (pid 1367)
                      0K: xtwifi-inet-agent (pid 1368)
                      0K: xtwifi-client (pid 1369)
                      0K: slim_daemon (pid 1370)
                      0K: sdcard (pid 2301)
                      0K: sdcard (pid 2318)
                      0K: wpa_supplicant (pid 2498)
                      0K: triton (pid 2808)
                      0K: lgvrhid (pid 2825)
                      0K: perfd (pid 2851)
                      0K: bnrd (pid 5816)
                      0K: logcat (pid 12551)
                      0K: dsqn (pid 13688)
                      0K: sh (pid 16709)
                      0K: dumpsys (pid 17005)
            141,459K: System
                139,067K: system (pid 1597)
                  2,392K: com.lge.signboard (pid 3248)
                      0K: com.lge.provider.signboard (pid 3372)
                      0K: com.lge.systemserver (pid 2570)
             19,368K: Persistent
                 19,368K: com.android.systemui (pid 2288)
                      0K: com.sktelecom.smartcard.SmartcardService (pid 3239)
                      0K: com.skt.skaf.OA00412131 (pid 3221)
                      0K: com.android.phone (pid 3207)
                      0K: com.lge.mlt (pid 3186)
                      0K: com.lge.ims (pid 3171)
                      0K: .dataservices (pid 3161)
                      0K: com.lge.wifi.p2p (pid 3149)
                      0K: com.lge.maagent (pid 3136)
                      0K: com.android.server.telecom (pid 3112)
                      0K: com.lge.wfds.service.v3 (pid 3103)
                      0K: com.android.nfc (pid 3076)
                      0K: com.lge.findlostphone (pid 3065)
                      0K: com.lge.musiccontroller (pid 3050)
                      0K: com.lge.lgdmprovisioningclient (pid 3044)
                      0K: com.lge.atservice (pid 2880)
                      0K: com.lge.myplace.engine (pid 2583)
             35,464K: Foreground
                 35,464K: com.lge.launcher3 (pid 4044 / activities)
                      0K: android.process.acore (pid 3407)
                      0K: com.lge.gametuner (pid 3297)
              3,432K: Perceptible
                  3,432K: com.lge.ime (pid 2283)
                      0K: com.lge.ime:com.rsupport.setting (pid 2377)
                788K: Cached
                    788K: com.google.android.apps.maps (pid 16500)
                      0K: com.android.defcontainer (pid 6342)
                      0K: com.lge.clock (pid 16948)
                      0K: com.google.process.gapps (pid 11197)
                      0K: com.android.vending (pid 15311)
                      0K: com.google.android.gms.ui (pid 16653)
                      0K: com.rsupport.rs.activity.rsupport (pid 16294)
                      0K: com.rsupport.rs.activity.aig (pid 16255)
                      0K: com.google.android.apps.photos (pid 16459)
                      0K: com.lge.lockscreensettings (pid 16207)
                      0K: com.lge.email (pid 16168)
                      0K: com.lge.provider.lockscreensettings (pid 16015)
                      0K: com.lge.coneshortcut (pid 15821)
                      0K: com.lge.conerecent (pid 16631)
                      0K: com.lge.appbox.client (pid 16148)
                      0K: com.android.printspooler (pid 16050)
                      0K: com.android.gallery3d (pid 16072)
                      0K: com.android.contacts (pid 16038)
                      0K: com.sec.android.app.sbrowser (pid 16335)
                      0K: com.rsupport.mobile.agent (pid 16532)
                      0K: com.wsandroid.suite.lge (pid 15127)

        Total PSS by category:
            185,492K: EGL mtrack
             66,200K: GL mtrack
             45,742K: Dalvik
             35,590K: Native
             19,805K: .dex mmap
             19,190K: .so mmap
              7,059K: .oat mmap
              6,716K: Ashmem
              5,805K: Dalvik Other
              3,255K: Unknown
              2,828K: Stack
              2,817K: .art mmap
                863K: .apk mmap
                473K: Other mmap
                148K: Gfx dev
                 76K: Other dev
                 15K: .ttf mmap
                  0K: Cursor
                  0K: .jar mmap
                  0K: Other mtrack

        Total RAM: 3,872,556K (status normal)
         Free RAM: 1,362,452K (      788K cached pss +   888,804K cached kernel +   472,860K free)
         Used RAM:   748,783K (  413,463K used pss +   335,320K kernel)
         Lost RAM: 1,744,334K
             ZRAM:    16,988K physical used for    53,724K in swap (  962,504K total swap)
           Tuning: 256 (large 512), oom   325,000K, restore limit   108,333K (high-end-gfx)
    """.trimIndent()

    @Mock
    lateinit var dumpsysReader: DumpsysReader

    @Before
    fun setup() {
        Mockito.`when`(dumpsysReader.read()).thenReturn(dumpSysMemInfo)
    }


    // 1. 문자를 line 단위로 읽는다.
    @Test
    fun readLineTest() = runBlocking {
        val stringReader = LineSplitter(dumpSysMemInfo)
        val lines = stringReader.getLines()
        MatcherAssert.assertThat("읽은 라인수가 달라서 실패", lines.size, Matchers.`is`(316))
    }

    // 2. 문자열에서 메모리 사용량을 찾아 long 으로 반환한다.
    @Test
    fun getMemoryUsageTest() = runBlocking {
        val textLine = "187,848K: surfaceflinger (pid 560)"
        val memoryUsage = MemoryUsageDumpsys(textLine)
        MatcherAssert.assertThat("메모리 사용량을 찾지 못해서 실패", memoryUsage.usageByte, Matchers.`is`(187848L * 1024))
    }

    // 3. 문자열에서 메모리 사용량을 찾아 long 으로 반환한다.(실패 케이스 1)
    @Test
    fun getMemoryUsageFailTest() = runBlocking {
        val textLine = "surfaceflinger (pid 560)"
        val memoryUsage = MemoryUsageDumpsys(textLine)
        MatcherAssert.assertThat("메모리 값이 없어야하는데 있어서 실패", memoryUsage.isAvailable, Matchers.`is`(false))
    }

    // 3-1. 문자열에서 메모리 사용량을 찾아 long 으로 반환한다.(실패 케이스 2)
    @Test
    fun getMemoryUsageFail2Test() = runBlocking {
        val textLine = "surfaceflinger:surfaceflinger (pid 560)"
        val memoryUsage = MemoryUsageDumpsys(textLine)
        MatcherAssert.assertThat("메모리 값이 없어야하는데 있어서 실패", memoryUsage.isAvailable, Matchers.`is`(false))
    }

    // 3-2. 문자열에서 메모리 사용량을 찾아 long 으로 반환한다.(실패 케이스 3)
    @Test
    fun getMemoryUsageFail3Test() = runBlocking {
        val textLine = ""
        val memoryUsage = MemoryUsageDumpsys(textLine)
        MatcherAssert.assertThat("메모리 값이 없어야하는데 있어서 실패", memoryUsage.isAvailable, Matchers.`is`(false))
    }

    // Package 이름을 찾는다.
    @Test
    fun getPackageNameTest() = runBlocking {
        val textLine = "187,848K: surfaceflinger (pid 560)"
        val memoryUsage = MemoryUsageDumpsys(textLine)
        MatcherAssert.assertThat("packageName 을 찾지 못해서 실패", memoryUsage.pkgName, Matchers.`is`("surfaceflinger"))
    }

    // Package 이름을 찾는다. ( PID 가 없을 경우 찾지 못하는 테스트 )
    @Test
    fun getPackageNameFailWhenNotFoundPIDTest() = runBlocking {
        val textLine = "187,848K: surfaceflinger"
        val memoryUsage = MemoryUsageDumpsys(textLine)
        MatcherAssert.assertThat("pid 가 없어서 찾지 못했어야하는데 성공해서 실패", memoryUsage.isAvailable, Matchers.`is`(false))
    }

    // Package 이름을 찾는다. (비어있는 문자열)
    @Test
    fun getPackageNameFailWhenEmptyTest() = runBlocking {
        val textLine = ""
        val memoryUsage = MemoryUsageDumpsys(textLine)
        MatcherAssert.assertThat("pid 가 없어서 찾지 못했어야하는데 성공해서 실패", memoryUsage.isAvailable, Matchers.`is`(false))
    }

    // 4. 문자열에서 pid 를 가져온다.(성공)
    @Test
    fun getMemoryUsagePidTest() = runBlocking {
        val textLine = "187,848K: surfaceflinger (pid 560)"
        val memoryUsage = MemoryUsageDumpsys(textLine)
        MatcherAssert.assertThat("pid 를 찾지 못해서 실패", memoryUsage.pid, Matchers.`is`("560"))
    }

    // 4. 문자열에서 pid 를 가져온다.(성공 1)
    @Test
    fun getMemoryUsagePid1Test() = runBlocking {
        val textLine = "35,464K: com.lge.launcher3 (pid 4044 / activities)"
        val memoryUsage = MemoryUsageDumpsys(textLine)
        MatcherAssert.assertThat("pid 를 찾지 못해서 실패", memoryUsage.pid, Matchers.`is`("4044"))
    }


    // 4-1. 문자열에서 pid 를 가져온다.(실패 케이스 1)
    @Test
    fun getMemoryUsagePidFail1Test() = runBlocking {
        val textLine = "187,848K: surfaceflinger (pid)"
        val memoryUsage = MemoryUsageDumpsys(textLine)
        MatcherAssert.assertThat("pid 를 찾아서 실패", memoryUsage.isAvailable, Matchers.`is`(false))
    }

    // 4-2. 문자열에서 pid 를 가져온다.(실패 케이스 2)
    @Test
    fun getMemoryUsagePidFail2Test() = runBlocking {
        val textLine = "187,848K: surfaceflinger"
        val memoryUsage = MemoryUsageDumpsys(textLine)
        MatcherAssert.assertThat("pid 를 찾아서 실패", memoryUsage.isAvailable, Matchers.`is`(false))
    }

    // 4-3. 문자열에서 pid 를 가져온다.(실패 케이스 3)
    @Test
    fun getMemoryUsagePidFail3Test() = runBlocking {
        val textLine = "35,464K: com.lge.launcher3 (pid / activities)"
        val memoryUsage = MemoryUsageDumpsys(textLine)
        MatcherAssert.assertThat("pid 를 찾아서 실패", memoryUsage.isAvailable, Matchers.`is`(false))
    }

    // 5. 메모리 정보를 hash map 에 저장(key: pid)하고 해당 key 의 메모리 정보가 있는지 확인한다.(pid 560)
    @Test
    fun getMemoryUsagesTest() = runBlocking {
        val memoryUsages = MemoryUsage.from(DumpsysMemoryUsageFactory(dumpsysReader))

        MatcherAssert.assertThat("메모리 용량이 달라서 실패", memoryUsages.find(KeyObject("surfaceflinger"))?.usageByte, Matchers.`is`(187848L * 1024))
    }

    // 5-1. 메모리 정보를 hash map 에 저장(key: pid)하고 해당 key 의 메모리 정보가 있는지 확인한다.(pid 4044)
    @Test
    fun getMemoryUsages4044Test() = runBlocking {
        val memoryUsages = MemoryUsage.from(DumpsysMemoryUsageFactory(dumpsysReader))
        MatcherAssert.assertThat("메모리 용량이 달라서 실패", memoryUsages.find(KeyObject("com.lge.launcher3"))?.usageByte, Matchers.`is`(35464L * 1024))
    }

    // 5-2. 메모리 정보를 hash map 에 저장(key: pid)하고 해당 key 의 메모리 정보가 있는지 확인한다.(pid 16038)
    @Test
    fun getMemoryUsages16038Test() = runBlocking {
        val memoryUsages = MemoryUsage.from(DumpsysMemoryUsageFactory(dumpsysReader))
        MatcherAssert.assertThat("메모리 용량이 달라서 실패", memoryUsages.find(KeyObject("com.android.contacts"))?.usageByte, Matchers.`is`(0L))
    }
}