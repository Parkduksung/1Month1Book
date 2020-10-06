package com.rsupport.mobile.agent.modules.sysinfo

import android.content.Context
import com.rsupport.knox.KnoxManagerCompat
import com.rsupport.knox.Updatable
import com.rsupport.mobile.agent.extension.guard
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.modules.engine.KnoxKeyUpdate
import com.rsupport.mobile.agent.modules.sysinfo.app.RunningAppFactory
import com.rsupport.mobile.agent.utils.SdkVersion
import com.rsupport.util.log.RLog
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.get
import org.koin.java.KoinJavaComponent.inject
import utils.checkSamsungDevice

class ApplicationInfoConnectedTest {

    private val KPE_LICENSE_KEY = "A08166C36E1E69D7D70B7E9A57A24B9D923B0A8E2A9BEF6EA11B673039CEC2A262A3DA7D61751024FDF59989ADA1DFD4BBD9806D80298ADD14F5A5134ADF49DA"
    private val context by inject(Context::class.java)
    private val engineTypeCheck by inject(EngineTypeCheck::class.java)
    private val runningAppFactory: RunningAppFactory by inject(RunningAppFactory::class.java)

    @Before
    fun setup() {
        engineTypeCheck.checkEngineType()
        loadKoinModules(module(override = true) {
            factory<Updatable>(named("knox")) {
                object : Updatable {
                    override fun update(): String {
                        return KPE_LICENSE_KEY
                    }
                }
            }
        })
    }

    @After
    fun tearDown() {
        loadKoinModules(module(override = true) { KnoxKeyUpdate() })
    }

    @Test
    fun knoxKoinReLoadTest() = runBlocking<Unit> {
        val updatable by inject(qualifier = named("knox"), clazz = Updatable::class.java)
        MatcherAssert.assertThat("", updatable.update(), Matchers.`is`(KPE_LICENSE_KEY))
    }

    @Test
    fun runningPkgTest() = runBlocking<Unit> {
        checkSamsungDevice().guard { return@runBlocking }

        val knoxCompat = KnoxManagerCompat().apply {
            knoxKeyUpdatable = get(qualifier = named("knox"), clazz = Updatable::class.java)
        }

        val isRunning = knoxCompat.isRunningPkg(context, context.packageName)
        MatcherAssert.assertThat("앱실행중이 아니라서 실패", isRunning, Matchers.`is`(true))
    }


    // Knox 사용중일때 실행중인 앱 pkg list 를 반환한다.
    @Test
    fun runningPkgListKnoxTest() = runBlocking {
        checkSamsungDevice().guard {
            RLog.w("삼성단말이 아니라서 실행하지 않음.")
            return@runBlocking
        }
        val runningAppManager = runningAppFactory.create()
        val runningAppInfos = runningAppManager.getRunningAppInfos()
        MatcherAssert.assertThat("knox 실행중인 프로세스를 찾지 못해서 실패", runningAppInfos.size, Matchers.greaterThan(1))
    }

    // Kitkat 이하 단말 사용중일때 실행중인 앱 pkg list 를 반환한다.(지원하지 않음)
    @Test
    fun runningPkgListKitkatTest() = runBlocking {
        (!SdkVersion().greaterThan21()).guard {
            RLog.w("OS 가 롤리팝이상이라서 실행하지 않음 ")
            return@runBlocking
        }
        val runningAppManager = runningAppFactory.create()
        val runningAppInfos = runningAppManager.getRunningAppInfos()
        MatcherAssert.assertThat("rsperm 이 실행중인 프로세스를 찾지 못해 실패", runningAppInfos.size, Matchers.greaterThan(1))
    }

//    // TODO ApplicationInfo 리펙토링 이후 테스트 코드 더 작성해야한다.
//    @Test
//    fun appInfoTest() = runBlocking {
//        val appInfo = ApplicationInfo(context)
//        val result = appInfo.loadApps(2)
//        MatcherAssert.assertThat("ApplicationInfo 생성에 실패", result, Matchers.notNullValue())
//    }
}
