package com.rsupport.mobile.agent.modules.sysinfo

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.spy
import com.rsupport.mobile.agent.TestApplication
import com.rsupport.mobile.agent.utils.IgnoreLogPrinter
import com.rsupport.util.log.RLog
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsString
import org.hamcrest.core.StringContains
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(
        application = TestApplication::class
)
@RunWith(RobolectricTestRunner::class)
class SystemInfoTest {

    private lateinit var context: Context
    private lateinit var systemInfo: SystemInfo

    @Before
    fun setup() {
        context = spy(ApplicationProvider.getApplicationContext<Context>())
        systemInfo = SystemInfo(context)
        RLog.setLogPrinter(IgnoreLogPrinter())
    }

    @After
    fun tearDown() {
        systemInfo.close()
    }

    @Test
    fun get_phone_number() {
        assertThat(systemInfo.systemInfo, StringContains.containsString("PhoneNumber&/"))
    }

    @Test
    fun get_empty_when_operator() {
        assertThat(systemInfo.systemInfo, StringContains.containsString("OperatorName&/"))
    }

    @Test
    fun get_rooting() {
        assertThat(systemInfo.systemInfo, StringContains.containsString("RootingInfo&/"))
    }

    @Test
    fun get_mobile_name() {
        assertThat(systemInfo.systemInfo, StringContains.containsString("ModelName&/"))
    }

    @Test
    fun get_firmware() {
        assertThat(systemInfo.systemInfo, StringContains.containsString("FirmwareVersion&/"))
    }

    @Test
    fun get_os_vsersion() {
        assertThat(systemInfo.systemInfo, StringContains.containsString("OSVersion&/"))
    }

    @Test
    fun get_battery_status() {
        assertThat(systemInfo.systemInfo, StringContains.containsString("BatteryStatus&/"))
    }

    @Test
    fun get_internal_storage_size() {
        assertThat(systemInfo.systemInfo, StringContains.containsString("InternalStorageSize&/"))
    }

    @Test
    fun get_external_storage_size() {
        assertThat(systemInfo.systemInfo, StringContains.containsString("ExternalStorageSize&/"))
    }

    @Test
    fun get_mobile_speed() {
        assertThat(systemInfo.systemInfo, StringContains.containsString("MobileSpeed&/"))
    }

    @Test
    fun get_wifi_speed() {
        assertThat(systemInfo.systemInfo, StringContains.containsString("WifiSpeed&/"))
    }

    @Test
    fun get_memory_state() {
        assertThat(systemInfo.systemInfo, StringContains.containsString("MemoryState&/"))
    }

    @Test
    fun get_simserial_number() {
        assertThat(systemInfo.systemInfo, StringContains.containsString("SIMSerialNumber&/"))
    }

    @Test
    fun get_build_number() {
        assertThat(systemInfo.systemInfo, StringContains.containsString("BuildNumber&/"))
    }

    @Test
    fun get_imei() {
        assertThat(systemInfo.systemInfo, StringContains.containsString("Imei&/"))
    }

    @Test
    fun get_language_set_state() {
        assertThat(systemInfo.systemInfo, StringContains.containsString("LanguageSetState&/"))
    }

    @Test
    fun get_apkversion() {
        assertThat(systemInfo.systemInfo, StringContains.containsString("APKVersion&/"))
    }

    @Test
    fun get_basic_info() = runBlocking<Unit> {
        val basicInfo = systemInfo.basicInfo
        assertThat(basicInfo, containsString("0&/"))
        assertThat(basicInfo, containsString("1&/"))
        assertThat(basicInfo, containsString("2&/"))
        assertThat(basicInfo, containsString("3&/"))
        assertThat(basicInfo, containsString("4&/"))
        assertThat(basicInfo, containsString("5&/"))
        assertThat(basicInfo, containsString("6&/"))
        assertThat(basicInfo, containsString("7&/"))
        assertThat(basicInfo, containsString("8&/"))
        assertThat(basicInfo, containsString("9&/"))
        assertThat(basicInfo, containsString("10&/"))
        assertThat(basicInfo, containsString("11&/"))
        assertThat(basicInfo, containsString("12&/"))
        assertThat(basicInfo, containsString("13&/"))
        assertThat(basicInfo, containsString("14&/"))
        assertThat(basicInfo, containsString("15&/"))
        assertThat(basicInfo, containsString("16&/"))

        assertThat(basicInfo, containsString("19&/"))
        assertThat(basicInfo, containsString("21&/"))
        assertThat(basicInfo, containsString("22&/"))
        assertThat(basicInfo, containsString("23&/"))
        assertThat(basicInfo, containsString("24&/"))
        assertThat(basicInfo, containsString("25&/"))
        assertThat(basicInfo, containsString("26&/"))
        assertThat(basicInfo, containsString("28&/"))
        assertThat(basicInfo, containsString("28&/"))
        assertThat(basicInfo, containsString("32&/"))
        assertThat(basicInfo, containsString("33&/"))
        assertThat(basicInfo, containsString("36&/"))
        assertThat(basicInfo, containsString("37&/"))

        assertThat(basicInfo, containsString("100&/"))
        assertThat(basicInfo, containsString("101&/"))
        assertThat(basicInfo, containsString("102&/"))
        assertThat(basicInfo, containsString("103&/"))
    }

    @Test
    fun get_setting_info() = runBlocking<Unit> {
        val settingInfo = systemInfo.settingInfo
        assertThat(settingInfo, containsString("0&/"))
        assertThat(settingInfo, containsString("1&/"))
        assertThat(settingInfo, containsString("2&/"))
        assertThat(settingInfo, containsString("3&/"))
        assertThat(settingInfo, containsString("5&/"))

    }
}