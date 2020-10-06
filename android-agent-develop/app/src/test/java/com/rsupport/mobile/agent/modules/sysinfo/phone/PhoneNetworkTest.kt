package com.rsupport.mobile.agent.modules.sysinfo.phone

import android.content.Context
import android.telephony.TelephonyManager
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.whenever
import com.rsupport.mobile.agent.TestApplication
import com.rsupport.mobile.agent.utils.TestLogPrinter
import com.rsupport.util.log.RLog
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(
        application = TestApplication::class
)
@RunWith(RobolectricTestRunner::class)
class PhoneNetworkTest {

    private lateinit var context: Context
    private lateinit var phoneNumber: PhoneNumber
    private lateinit var telephonyManager: TelephonyManager

    private lateinit var phoneNetwork: PhoneNetwork

    @Before
    fun setup() {
        RLog.setLogPrinter(TestLogPrinter())
        context = spy(ApplicationProvider.getApplicationContext<Context>())
        telephonyManager = mock(TelephonyManager::class.java)
        phoneNumber = mock(PhoneNumber::class.java)
    }

    @Test
    fun get_empty_operator_name() {
        phoneNetwork = PhoneNetwork(context)

        val operatorName = phoneNetwork.getOperatorName()

        assertThat(operatorName, `is`("EMPTY"))
    }

    @Test
    fun get_sim_operator_name() {
        givenSimOperatorName("KT")

        phoneNetwork = PhoneNetwork(context)
        val operatorName = phoneNetwork.getOperatorName()
        assertThat(operatorName, `is`("KT"))
    }

    @Test
    fun get_service_sim_operator_name() {
        val phoneStateProvider = givenPhoneStateProvider("KT")

        phoneNetwork = PhoneNetwork(context, phoneStateProvider = phoneStateProvider)
        val operatorName = phoneNetwork.getOperatorName()
        assertThat(operatorName, `is`("KT"))
    }


    @Test
    fun get_empty_mobile_speed() {
        givenNotSimState()

        phoneNetwork = PhoneNetwork(context)
        val operatorName = phoneNetwork.getMobileSpeed()
        assertThat(operatorName, `is`("EMPTY"))
    }

    @Test
    fun get_mobile_speed() {
        phoneNetwork = PhoneNetwork(context)
        val operatorName = phoneNetwork.getMobileSpeed()
        assertThat(operatorName, `is`("0%"))
    }


    @Test
    fun close() {
        phoneNetwork = PhoneNetwork(context)
        phoneNetwork.close()
    }


    private fun givenNotSimState() {
        whenever(telephonyManager.simState).thenReturn(TelephonyManager.SIM_STATE_ABSENT)
        whenever(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).thenReturn(telephonyManager)
    }

    private fun givenPhoneStateProvider(name: String): PhoneStateProvider {
        val phoneStateProvider = mock(PhoneStateProvider::class.java)

        whenever(phoneStateProvider.getOperatorName()).thenReturn(name)
        return phoneStateProvider
    }


    private fun givenSimOperatorName(name: String): String {
        whenever(telephonyManager.simOperatorName).thenReturn(name)
        whenever(context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).thenReturn(telephonyManager)
        return name
    }
}