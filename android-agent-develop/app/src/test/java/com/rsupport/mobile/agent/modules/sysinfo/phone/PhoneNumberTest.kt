package com.rsupport.mobile.agent.modules.sysinfo.phone

import android.content.Context
import android.telephony.TelephonyManager
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.whenever
import com.rsupport.mobile.agent.TestApplication
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(
        application = TestApplication::class
)
@RunWith(RobolectricTestRunner::class)
class PhoneNumberTest {

    private lateinit var context: Context
    private lateinit var phoneNumber: PhoneNumber
    private val telephonyManager = Mockito.mock(TelephonyManager::class.java)

    @Before
    fun setup() {
        context = Mockito.spy(ApplicationProvider.getApplicationContext<Context>())
        phoneNumber = PhoneNumber(context)
    }

    @Test
    fun get_empty_phone_number_when_not_support() {
        val phoneNumber = phoneNumber.getPhoneNumber()
        MatcherAssert.assertThat(phoneNumber, Matchers.`is`("EMPTY"))
    }

    @Test
    fun get_phone_number_when_not_kr() {
        val resultPhoneNumber = givenPhoneNumber("123456")

        val phoneNumber = phoneNumber.getPhoneNumber()
        MatcherAssert.assertThat(phoneNumber, Matchers.`is`(resultPhoneNumber))
    }


    @Test
    fun get_phone_number_when_kr() {
        val resultPhoneNumber = givenPhoneNumber("82123456")
        whenever(telephonyManager.networkOperator).thenReturn("450")

        val phoneNumber = phoneNumber.getPhoneNumber()
        MatcherAssert.assertThat(phoneNumber, Matchers.`is`("0123456"))
    }


    private fun givenPhoneNumber(number: String): String {
        whenever(telephonyManager.line1Number).thenReturn(number)
        whenever(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(telephonyManager)
        return number
    }

}