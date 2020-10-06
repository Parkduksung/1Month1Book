package com.rsupport.mobile.agent.ui.test.utils

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import com.rsupport.mobile.agent.R
import org.hamcrest.Matchers

class InstallUtils {


    fun setup() {
    }

    fun tearDown() {
    }

    // install
    fun performInstall(agentName: String = "unit Test Agent") {
        performAgentInstallInfo(agentName)
        performAgentInstallClick()

        // agentInfo - 설치완료 팝업
        Espresso.onView(ViewMatchers.withText(R.string.re_common_ok))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click())
    }

    // agentInfo - 로그인 상태 확인
    fun verifyAgentInfoActivity() {
        Espresso.onView(ViewMatchers.withId(R.id.btn_login))
                .check(ViewAssertions.matches(ViewMatchers.withText(R.string.logout)))
    }


    private fun performAgentInstallClick() {
        Espresso.onView(ViewMatchers.withId(R.id.bottom_title_layout)).perform(ViewActions.click())
    }


    private fun performAgentInstallInfo(agentName: String) {
        Espresso.onView(ViewMatchers.withId(R.id.about_list1)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.about_list1))
                .atPosition(0)
                .onChildView(ViewMatchers.withId(R.id.item_content_edit))
                .perform(ViewActions.replaceText(agentName))

        Espresso.closeSoftKeyboard()

        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.about_list1))
                .atPosition(2)
                .onChildView(ViewMatchers.withId(R.id.item_content_edit))
                .perform(ViewActions.typeText("aaaaaa"))
        Espresso.closeSoftKeyboard()


        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.about_list1))
                .atPosition(3)
                .onChildView(ViewMatchers.withId(R.id.item_content_edit))
                .perform(ViewActions.typeText("111111"))
        Espresso.closeSoftKeyboard()

        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.about_list1))
                .atPosition(4)
                .onChildView(ViewMatchers.withId(R.id.item_content_edit))
                .perform(ViewActions.typeText("111111"))
        Espresso.closeSoftKeyboard()
    }

}