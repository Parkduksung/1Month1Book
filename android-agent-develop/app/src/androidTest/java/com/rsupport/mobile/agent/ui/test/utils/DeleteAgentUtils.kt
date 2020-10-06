package com.rsupport.mobile.agent.ui.test.utils

import android.content.Context
import android.text.format.DateUtils
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import com.rsupport.mobile.agent.constant.ComConstant
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.error.ErrorCode
import org.hamcrest.Matchers
import org.hamcrest.core.StringContains
import org.koin.java.KoinJavaComponent.inject

class DeleteAgentUtils {
    private val CORRECT_USER_ID = "kwcho"
    private val CORRECT_USER_PWD = "111111"

    private val loginUtils = LoginUtils()
    private val installUtils = InstallUtils()
    private val context by inject(Context::class.java)

    suspend fun setup() {
        loginUtils.setup()
        installUtils.setup()
    }

    suspend fun tearDown() {
        loginUtils.tearDown()
        installUtils.tearDown()
    }

    suspend fun performLoginInstall() {
        loginUtils.launch()
        loginUtils.performLogin()
        installUtils.performInstall("deleted.${createDeviceName()}")
    }

    suspend fun performDelete(userId: String = CORRECT_USER_ID, userPwd: String = CORRECT_USER_PWD) {
        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.agentinfo_list1))
                .atPosition(2)
                .onChildView(ViewMatchers.withId(R.id.imgBtn))
                .perform(ViewActions.click())


        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.agentinfo_list2))
                .atPosition(2)
                .onChildView(ViewMatchers.withId(R.id.imgBtn))
                .perform(ViewActions.click())


        setupUserId(userId)
        setupUserPwd(userPwd)

        performDeleteClick()
    }

    private fun createDeviceName(): String {
        return DateUtils.formatDateTime(context, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME).let {
            Regex("[:-]").replace(it, ".")
        }
    }

    suspend fun verifyLoginActivity() {
        loginUtils.verifyLoginActivity()
    }

    private fun performDeleteClick() {
        Espresso.onView(ViewMatchers.withId(R.id.btn_delete)).perform(ViewActions.click())
    }

    private fun setupUserPwd(userPwd: String) {
        Espresso.onView(ViewMatchers.withId(R.id.userpasswd)).perform(ViewActions.typeText(userPwd))
        Espresso.closeSoftKeyboard()
    }

    private fun setupUserId(userId: String) {
        Espresso.onView(ViewMatchers.withId(R.id.userid)).perform(ViewActions.typeText(userId))
        Espresso.closeSoftKeyboard()
    }

    fun verifyAgentInfo() {
        installUtils.verifyAgentInfoActivity()
    }

    fun performSetting() {
        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.agentinfo_list1))
                .atPosition(2)
                .onChildView(ViewMatchers.withId(R.id.imgBtn))
                .perform(ViewActions.click())
    }

    fun performConfiguration() {
        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.agentinfo_list2))
                .atPosition(0)
                .onChildView(ViewMatchers.withId(R.id.imgBtn))
                .perform(ViewActions.click())
    }

    fun performProxyOn() {
        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.about_list1))
                .atPosition(2)
                .onChildView(ViewMatchers.withId(R.id.imgBtn))
                .perform(ViewActions.click())

    }

    fun performBack() {
        Espresso.onView(ViewMatchers.withId(R.id.left_button)).perform(click())
    }

    fun verifyProxyConfig() {
        Espresso.onView(ViewMatchers.withId(R.id.tvcontent))
                .inRoot(RootMatchers.isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(StringContains.containsString(ErrorCode.SETTING_NET_ERR_PROXY_VERIFY.toString()))))
    }

}