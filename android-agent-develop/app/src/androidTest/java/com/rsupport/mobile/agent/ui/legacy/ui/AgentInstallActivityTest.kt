package com.rsupport.mobile.agent.ui.legacy.ui

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.rsupport.mobile.agent.utils.SdkVersion
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.ui.agent.agentinfo.AgentInfoInteractor
import com.rsupport.mobile.agent.ui.login.LoginActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.anything
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject
import org.mockito.Mockito

@LargeTest
@RunWith(AndroidJUnit4::class)
class AgentInstallActivityTest {
    private val agentInfoInteractor by inject(AgentInfoInteractor::class.java)
    private val configRepository by inject(ConfigRepository::class.java)

    companion object {
        private const val COLLECT_CORP_ID = "kwcho"
        private const val COLLECT_ID = "kwcho"
        private const val COLLECT_PWD = "111111"

    }

    @Before
    fun setup() = runBlocking<Unit> {
        unloadKoinModules(
                module { single { SdkVersion() } }
        )

        loadKoinModules(
                module { single { Mockito.spy(SdkVersion()) } }
        )

        inject(SdkVersion::class.java).value.apply {
            Mockito.`when`(greaterThan23()).thenReturn(false)
            Mockito.`when`(greaterThan21()).thenReturn(true)
        }

        agentInfoInteractor.removeAgent()
        configRepository.setShowTutorial(true)

        ActivityScenario.launch(LoginActivity::class.java)
    }

    @After
    fun tearDown() {
        unloadKoinModules(
                module { single { SdkVersion() } }
        )

        loadKoinModules(
                module { single { SdkVersion() } }
        )
    }

    @Test
    fun agentInstallTest() = runBlocking<Unit> {

        onView(withId(R.id.corpid)).check(matches(isDisplayed()));
        onView(withId(R.id.corpid)).perform(ViewActions.typeText(COLLECT_CORP_ID))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.userid)).perform(ViewActions.typeText(COLLECT_ID))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.userpasswd)).perform(ViewActions.typeText(COLLECT_PWD))
        Espresso.closeSoftKeyboard()

        onView(withId(R.id.loginButton)).perform(ViewActions.click())

        onView(withId(R.id.about_list1)).check(matches(isDisplayed()));

        Espresso.onData(anything())
                .inAdapterView(withId(R.id.about_list1))
                .atPosition(0)
                .onChildView(withId(R.id.item_content_edit))
                .perform(ViewActions.typeText("unitTest agent"))
        Espresso.closeSoftKeyboard()

        Espresso.onData(anything())
                .inAdapterView(withId(R.id.about_list1))
                .atPosition(2)
                .onChildView(withId(R.id.item_content_edit))
                .perform(ViewActions.typeText("aaaaaa"))
        Espresso.closeSoftKeyboard()


        Espresso.onData(anything())
                .inAdapterView(withId(R.id.about_list1))
                .atPosition(3)
                .onChildView(withId(R.id.item_content_edit))
                .perform(ViewActions.typeText("111111"))
        Espresso.closeSoftKeyboard()

        Espresso.onData(anything())
                .inAdapterView(withId(R.id.about_list1))
                .atPosition(4)
                .onChildView(withId(R.id.item_content_edit))
                .perform(ViewActions.typeText("111111"))
        Espresso.closeSoftKeyboard()


        onView(withId(R.id.bottom_title_layout)).perform(ViewActions.click())


        // agentInfo - 설치완료 팝업
        onView(ViewMatchers.withText(R.string.re_common_ok))
                .inRoot(RootMatchers.isDialog())
                .perform(ViewActions.click())

        // agentInfo - 로그인 상태 확인
        onView(withId(R.id.btn_login))
                .check(matches(ViewMatchers.withText(R.string.logout)))


    }
}