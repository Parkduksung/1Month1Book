package com.rsupport.mobile.agent.modules.sysinfo.app

import android.app.ActivityManager
import android.content.pm.PackageManager
import base.BaseTest
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.rsupport.knox.KnoxManagerCompat
import com.rsupport.rsperm.IRSPerm
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Mockito

class RunningApplicationTest : BaseTest() {

    @Mock
    lateinit var activityManager: ActivityManager

    @Mock
    lateinit var packageManager: PackageManager

    @Mock
    lateinit var knoxManagerCompat: KnoxManagerCompat

    override fun createModules(): List<Module> {
        return listOf(module {
            single(override = true) { activityManager }
            single(override = true) { packageManager }
            single { knoxManagerCompat }
        })
    }


    // Knox 사용중일때 실행중인 앱 pkg list 를 반환한다.
    @Test
    fun runningPkgListKnoxTest() = runBlocking {
        Mockito.`when`(packageManager.getInstalledApplications(any())).thenReturn(
                listOf(
                        android.content.pm.ApplicationInfo().apply { packageName = "com.rsupport.test" },
                        android.content.pm.ApplicationInfo().apply { packageName = "com.rsupport.test2" },
                        android.content.pm.ApplicationInfo().apply { packageName = "com.rsupport.mobile.agent" }
                )
        )
        Mockito.`when`(knoxManagerCompat.isRunningPkg(any(), eq("com.rsupport.mobile.agent"))).thenReturn(true)

        val runningApplication: RunningApplication = RunningApplicationKnox()
        val runningAppInfos = runningApplication.getRunningAppInfos()
        MatcherAssert.assertThat("knox 실행중인 프로세스를 찾지 못해서 실패", runningAppInfos.size, Matchers.`is`(1))
    }


    // rsperm 사용중일때 실행중인 앱 pkg list 를 반환한다.(RSPerm 과 바인딩을할 수 없다....)
    @Test
    fun runningPkgListRspermTest() = runBlocking {
        val mockRsperm = Mockito.mock(IRSPerm::class.java)
        Mockito.`when`(mockRsperm.runningProcesses).thenReturn(
                listOf(
                        ActivityManager.RunningAppProcessInfo().apply { processName = "com.rsupport.test1" },
                        ActivityManager.RunningAppProcessInfo().apply { processName = "com.rsupport.test2" },
                        ActivityManager.RunningAppProcessInfo().apply { processName = "com.rsupport.mobile.agent" }
                )
        )

        val runningApplication: RunningApplication = RunningApplicationRsperm(mockRsperm)
        val runningAppInfos = runningApplication.getRunningAppInfos()
        MatcherAssert.assertThat("rsperm 이 실행중인 프로세스를 찾지 못해 실패", runningAppInfos.size, Matchers.`is`(3))
    }


    // Kitkat 이하 단말 사용중일때 실행중인 앱 pkg list 를 반환한다.(지원하지 않음)
    @Test
    fun runningPkgListKitkatTest() = runBlocking {
        Mockito.`when`(activityManager.runningAppProcesses).thenReturn(
                listOf(
                        ActivityManager.RunningAppProcessInfo().apply { processName = "com.rsupport.test1" },
                        ActivityManager.RunningAppProcessInfo().apply { processName = "com.rsupport.test2" },
                        ActivityManager.RunningAppProcessInfo().apply { processName = "com.rsupport.mobile.agent" }
                )
        )
        val runningApplication: RunningApplication = RunningApplicationKitkat()
        val runningAppInfos = runningApplication.getRunningAppInfos()
        MatcherAssert.assertThat("rsperm 이 실행중인 프로세스를 찾지 못해 실패", runningAppInfos.size, Matchers.`is`(3))
    }
}

