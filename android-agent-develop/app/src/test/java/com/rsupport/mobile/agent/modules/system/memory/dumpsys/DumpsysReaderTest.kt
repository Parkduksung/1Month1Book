package com.rsupport.mobile.agent.modules.system.memory.dumpsys

import base.BaseTest
import com.nhaarman.mockitokotlin2.any
import com.rsupport.litecam.binder.Binder
import com.rsupport.mobile.agent.modules.memory.dumpsys.DumpsysReader
import com.rsupport.mobile.agent.service.RSPermService
import com.rsupport.rsperm.IRSPerm
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.FileInputStream

@RunWith(MockitoJUnitRunner::class)
class DumpsysReaderTest : BaseTest() {

    @Mock
    lateinit var rsperm: IRSPerm

    @Mock
    lateinit var rspermBinder: Binder

    override fun createModules(): List<Module> {
        return listOf(
                module {
                    factory { rspermBinder }
                    factory { RSPermService() }
                }
        )
    }

    private val dummyString = """
        Applications Memory Usage (in Kilobytes):
        Uptime: 22638691 Realtime: 22638691

        Total PSS by process:
            187,848K: surfaceflinger (pid 560)
            143,766K: system (pid 1597)
             35,464K: com.lge.launcher3 (pid 4044 / activities)
             19,368K: com.android.systemui (pid 2288)
              6,845K: mm-pp-dpps (pid 601)
              6,788K: qseeproxydaemon (pid 941)
              3,078K: audiod (pid 948)
              2,392K: com.lge.signboard (pid 3248)
                965K: pm-service (pid 537)
                925K: ATFWD-daemon (pid 940)
                913K: fingerprintd (pid 946)
                801K: fidod (pid 952)
                773K: pm-proxy (pid 580)
                715K: servicemanager (pid 559)
                664K: lgsecclkserver (pid 802)
                586K: lghashstorageserver (pid 839)
                583K: gbmd (pid 810)
                579K: prmd (pid 811)
                577K: ccmd (pid 557)
                527K: subsystem_ramdump (pid 579)
                356K: ipsecstarter (pid 820)
                240K: mcDriverDaemon (pid 758)
                  0K: com.google.android.googlequicksearchbox:search (pid 2942)
                  0K: com.android.defcontainer (pid 22981)
                  0K: com.skt.skaf.A000Z00040 (pid 3940)
                  0K: com.android.vending (pid 24433)
                  0K: com.google.android.gms.persistent (pid 2553)
                  0K: com.lge.coneshortcut (pid 24537)
                  0K: com.android.vending:download_service (pid 24569)
                  0K: com.google.process.gapps (pid 11197)
                  0K: com.google.android.videos (pid 24404)
                  0K: com.lge.wifisettings (pid 23679)
                  0K: com.wsandroid.suite.lge (pid 24264)
                  0K: com.lge.lgdmsclient (pid 24319)
                  0K: android.process.acore (pid 3407)
                  0K: com.lge.clock (pid 23355)
                  0K: com.google.android.gms (pid 2979)
                  0K: com.google.android.talk (pid 24117)
                  0K: com.google.android.apps.photos (pid 24064)
                  0K: com.google.android.youtube (pid 7707)
                  0K: com.lge.iftttmanager (pid 23502)
                  0K: com.lge.ime:com.rsupport.setting (pid 23140)
                  0K: com.lge.ime (pid 23124)
                  0K: android.process.media (pid 3625)
                  0K: com.skt.skaf.OA00199800 (pid 4372)
                  0K: com.lge.gametuner (pid 3297)
                  0K: com.lge.provider.signboard (pid 3372)
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
                  0K: dumpsys (pid 24697)

        Total PSS by OOM adjustment:
            213,763K: Native
                187,848K: surfaceflinger (pid 560)
                  6,845K: mm-pp-dpps (pid 601)
                  6,788K: qseeproxydaemon (pid 941)
                  3,078K: audiod (pid 948)
                    965K: pm-service (pid 537)
                    925K: ATFWD-daemon (pid 940)
                    913K: fingerprintd (pid 946)
                    801K: fidod (pid 952)
                    773K: pm-proxy (pid 580)
                    715K: servicemanager (pid 559)
                    664K: lgsecclkserver (pid 802)
                    586K: lghashstorageserver (pid 839)
                    583K: gbmd (pid 810)
                    579K: prmd (pid 811)
                    577K: ccmd (pid 557)
                    527K: subsystem_ramdump (pid 579)
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
                      0K: dumpsys (pid 24697)
            146,158K: System
                143,766K: system (pid 1597)
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

        Total PSS by category:
            185,492K: EGL mtrack
             61,980K: GL mtrack
             51,990K: Dalvik
             32,112K: Native
             20,372K: .dex mmap
             16,693K: .so mmap
              7,413K: .oat mmap
              6,714K: Ashmem
              6,667K: Dalvik Other
              2,922K: .art mmap
              2,741K: Stack
              2,738K: Unknown
                460K: Other mmap
                244K: .apk mmap
                148K: Gfx dev
                 76K: Other dev
                 16K: .ttf mmap
                  0K: Cursor
                  0K: .jar mmap
                  0K: Other mtrack

        Total RAM: 3,872,556K (status normal)
         Free RAM: 1,486,724K (        0K cached pss + 1,313,904K cached kernel +   172,820K free)
         Used RAM:   739,529K (  414,753K used pss +   324,776K kernel)
         Lost RAM: 1,625,242K
             ZRAM:    21,068K physical used for    66,244K in swap (  962,504K total swap)
           Tuning: 256 (large 512), oom   325,000K, restore limit   108,333K (high-end-gfx)

    """.trimIndent()

    @Test
    fun readStringByRspermTest() = runBlocking {
        val fileInputStream = FileInputStream("./src/test/file/dumpsys_meminfo.log")

        Mockito.`when`(rspermBinder.isBinderAlive).thenReturn(true)
        Mockito.`when`(rspermBinder.binder).thenReturn(rsperm)
        Mockito.`when`(rsperm.getFile(any())).thenReturn(fileInputStream.fd)
        Mockito.`when`(rsperm.hasDumpsys()).thenReturn(true)

        val stringReader = DumpsysReader.createMemInfo()
        val readString = stringReader.read()
        MatcherAssert.assertThat("String 을 읽지 못해서 실패", readString, Matchers.`is`(dummyString))
    }

    @Test
    fun readStringByRspermFailTest() = runBlocking {
        Mockito.`when`(rspermBinder.isBinderAlive).thenReturn(true)
        Mockito.`when`(rspermBinder.binder).thenReturn(rsperm)
        Mockito.`when`(rsperm.hasDumpsys()).thenReturn(false)

        val stringReader = DumpsysReader.createMemInfo()
        val readString = stringReader.read()
        MatcherAssert.assertThat("String 을 읽어서 실패", readString, Matchers.`is`(""))
    }

    @Test
    fun readStringByRspermDeadFailTest() = runBlocking {
        Mockito.`when`(rspermBinder.isBinderAlive).thenReturn(false)

        val stringReader = DumpsysReader.createMemInfo()
        val readString = stringReader.read()
        MatcherAssert.assertThat("String 을 읽어서 실패", readString, Matchers.`is`(""))
    }
}