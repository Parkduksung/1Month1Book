package com.rsupport.engine

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.rsupport.mobile.agent.modules.engine.EngineType
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.java.KoinJavaComponent.inject
import org.mockito.junit.MockitoJUnitRunner
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.constant.PreferenceConstant

@RunWith(MockitoJUnitRunner::class)
@LargeTest
class EngineTypeTest {

    lateinit var context: Context
    private val engineTypeCheck by inject(EngineTypeCheck::class.java)

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        GlobalStatic.loadDeviceInfo()
        GlobalStatic.loadAppInfo(context)
        GlobalStatic.loadResource(context)
        loadSettingInfo(context)
    }

    private fun loadSettingInfo(context: Context) {
        val pref = context.getSharedPreferences(PreferenceConstant.RV_PREF_GUIDE_VIEW, Activity.MODE_PRIVATE)
        GlobalStatic.ISTOUCHVIEWGUIDE = pref.getBoolean("istouchviewguide", true)
        GlobalStatic.ISCURSORVIEWGUIDE = pref.getBoolean("iscursorviewguide", true)
        GlobalStatic.g_setinfoLanguage = GlobalStatic.getSystemLanguage(context)
        GlobalStatic.loadSettingURLInfo(context)
    }


    // 1. 사용 가능한 Engine을 확인한다. (MainThread)
    @Test
    fun checkAvailableEngineMainTest() = runBlocking {
        withContext(Dispatchers.Main) {
            engineTypeCheck.checkEngineType()
            val engineType = engineTypeCheck.getEngineType()

            if ("samsung" == Build.MANUFACTURER) {
                MatcherAssert.assertThat("Knox 가 아니어서 실패", engineType, Matchers.`is`(EngineType.ENGINE_TYPE_KNOX))
            } else {
                MatcherAssert.assertThat("Knox 가 아니어서 실패", engineType, Matchers.not(EngineType.ENGINE_TYPE_KNOX))
            }
        }
    }

    // 2. 사용 가능한 Engine을 확인한다. (IO Thread)
    @Test
    fun checkAvailableEngineIOTest() = runBlocking {
        withContext(Dispatchers.IO) {
            engineTypeCheck.checkEngineType()
            val engineType = engineTypeCheck.getEngineType()
            if ("samsung" == Build.MANUFACTURER) {
                MatcherAssert.assertThat("Knox 가 아니어서 실패", engineType, Matchers.`is`(EngineType.ENGINE_TYPE_KNOX))
            } else {
                MatcherAssert.assertThat("Knox 가 아니어서 실패", engineType, Matchers.not(EngineType.ENGINE_TYPE_KNOX))
            }
        }
    }
}