package com.rsupport.mobile.agent.ui.agent.install

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.ui.agent.agentinfo.AgentInfoInteractor
import com.rsupport.mobile.agent.ui.login.LoginActivity
import com.rsupport.mobile.agent.utils.SdkVersion
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers
import org.hamcrest.core.StringContains
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.get
import org.koin.java.KoinJavaComponent.inject
import org.mockito.Mockito

class AgentInstallActivityTest {

    companion object {
        private const val COLLECT_CORP_ID = "kwcho"
        private const val COLLECT_ID = "kwcho"
        private const val COLLECT_PWD = "111111"

    }

    private val agentInfoInteractor by inject(AgentInfoInteractor::class.java)
    private val configRepository by inject(ConfigRepository::class.java)

    @Before
    fun setup() = runBlocking<Unit> {
        loadKoinModules(module(override = true) { single { Mockito.spy(SdkVersion()) } })
        get(SdkVersion::class.java).apply {
            Mockito.`when`(greaterThan23()).thenReturn(false)
            Mockito.`when`(greaterThan21()).thenReturn(true)
        }

        agentInfoInteractor.removeAgent()
        configRepository.setShowTutorial(true)
    }

    @After
    fun tearDown() = runBlocking<Unit> {
        loadKoinModules(module(override = true) { single { SdkVersion() } })
    }

    // AgentInstall 에서 Group 선택후 회전했을때 선택이 유지되는지를 확인한다.
    @Test
    fun maintainSelectedGroupWhenRotationTest() = runBlocking<Unit> {
        ActivityScenario.launch(LoginActivity::class.java)

        // 로그인
        Espresso.onView(ViewMatchers.withId(R.id.corpid)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        Espresso.onView(ViewMatchers.withId(R.id.corpid)).perform(ViewActions.typeText(COLLECT_CORP_ID))
        Espresso.closeSoftKeyboard()
        Espresso.onView(ViewMatchers.withId(R.id.userid)).perform(ViewActions.typeText(COLLECT_ID))
        Espresso.closeSoftKeyboard()
        Espresso.onView(ViewMatchers.withId(R.id.userpasswd)).perform(ViewActions.typeText(COLLECT_PWD))
        Espresso.closeSoftKeyboard()
        Espresso.onView(ViewMatchers.withId(R.id.loginButton)).perform(ViewActions.click())

        // 그룹 선택
        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.about_list1))
                .atPosition(1)
                .onChildView(ViewMatchers.withId(R.id.imgBtn))
                .perform(ViewActions.click())


        // 그룹 선택 > child 1
        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.group_list))
                .atPosition(0)
                .perform(ViewActions.click())


        // 그룹 선택 > child 1 > check
        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.group_list))
                .atPosition(0)
                .onChildView(ViewMatchers.withId(R.id.img_folder_chek))
                .perform(ViewActions.click())

        // 선택 완료
        Espresso.onView(ViewMatchers.withId(R.id.group_select)).perform(ViewActions.click())


        // 그룹 선택
        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.about_list1))
                .atPosition(1)
                .onChildView(ViewMatchers.withId(R.id.item_content))
                .check(ViewAssertions.matches(ViewMatchers.withText(StringContains.containsString("aosp"))))

        // 회전
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).setOrientationLeft()

        // 그룹 선택
        Espresso.onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.about_list1))
                .atPosition(1)
                .onChildView(ViewMatchers.withId(R.id.item_content))
                .check(ViewAssertions.matches(ViewMatchers.withText(StringContains.containsString("aosp"))))

        // 회전
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).setOrientationNatural()
    }
}