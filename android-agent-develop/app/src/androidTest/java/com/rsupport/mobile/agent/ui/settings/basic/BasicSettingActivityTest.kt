package com.rsupport.mobile.agent.ui.settings.basic

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.espresso.matcher.ViewMatchers
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.repo.config.ProxyInfo
import kotlinx.coroutines.runBlocking
import org.hamcrest.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.java.KoinJavaComponent.inject
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BasicSettingActivityTest {

    @get: Rule
    val intentsTestRule = IntentsTestRule(BasicSettingActivity::class.java)

    private val context by inject(Context::class.java)
    private val configRepository by inject(ConfigRepository::class.java)

    @Before
    fun setup() {
        configRepository.delete()
    }


    @After
    fun tearDown() = runBlocking<Unit> {
        configRepository.delete()
    }

    // 새로운 공지사항이 있을경우 아이콘이 보이는지 확인한다.
    @Test
    fun showNoticeIconWhenNewTest() = runBlocking<Unit> {
        configRepository.setNewNoticeSeq(1)

        ActivityScenario.launch(BasicSettingActivity::class.java)

        Espresso.onView(ViewMatchers.withId(R.id.newNoticeImageView))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }


    // 기본정보 > 라이선스 activity 로 이동하는지를 확인한다.
    @Test
    fun launchLicensesTest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)

        Espresso.onView(ViewMatchers.withId(R.id.licenseLayout)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.common_state))
                .check(ViewAssertions.matches(ViewMatchers.withText(R.string.license)))
    }

    // 기본정보 > 튜토리얼 activity 로 이동하는지를 확인한다.
    @Test
    fun launchTutorialTest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)

        Espresso.onView(ViewMatchers.withId(R.id.tutorialLayout)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.common_state))
                .check(ViewAssertions.matches(ViewMatchers.withText(R.string.tutorial_title)))

    }

    // 기본정보 > 약관 activity 로 이동하는지를 확인한다.
    @Test
    fun launchTermsTest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)

        Espresso.onView(ViewMatchers.withId(R.id.termsLayout)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.common_state))
                .check(ViewAssertions.matches(ViewMatchers.withText(R.string.terms_of_use)))

    }

    // 기본정보 > 개인정보 activity 로 이동하는지를 확인한다.
    @Test
    fun launchPrivacyTest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)

        Espresso.onView(ViewMatchers.withId(R.id.privacyLayout)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.common_state))
                .check(ViewAssertions.matches(ViewMatchers.withText(R.string.privacy_policy)))

    }


    // 기본정보 > 공지 activity 로 이동하는지를 확인한다.
    @Test
    fun launchNoticeTest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)

        Espresso.onView(ViewMatchers.withId(R.id.noticeLayout)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.common_state))
                .check(ViewAssertions.matches(ViewMatchers.withText(R.string.about_notice)))

    }

    // 기본정보 > FAQ activity 로 이동하는지를 확인한다.
    @Test
    fun launchFAQTest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)

        Espresso.onView(ViewMatchers.withId(R.id.faqLayout)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.common_state))
                .check(ViewAssertions.matches(ViewMatchers.withText(R.string.string_faq)))

    }

    // 환경설정 > 사용 제품 선택시 expanded 테스트
    @Test
    fun selectCorpProductTest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)

        Espresso.onView(ViewMatchers.withId(R.id.productSelectLayout)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.standardServerLayout)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.customServerLayout)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    // 환경설정 > 사용 제품 선택시 expanded > collapse 테스트
    @Test
    fun selectCorpProductExpandCollapseTest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)
        Espresso.onView(ViewMatchers.withId(R.id.productSelectLayout)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.standardServerLayout)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.customServerLayout)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        Espresso.onView(ViewMatchers.withId(R.id.productSelectLayout)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.standardServerLayout)).check(ViewAssertions
                .matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        Espresso.onView(ViewMatchers.withId(R.id.customServerLayout)).check(ViewAssertions
                .matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    // 환경설정 > Standard/ Enterprise 제품 선택시 선택 URL 확인
    @Test
    fun selectCorpProductURLTest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)

        Espresso.onView(ViewMatchers.withId(R.id.productSelectLayout)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.standardServerLayout)).perform(ViewActions.click())

        val serverInfo = configRepository.getServerInfo()
        MatcherAssert.assertThat("서버 정보가 잘못되서 실패", serverInfo.url, Matchers.`is`(context.getString(R.string.serverip_biz)));
    }

    // 환경설정 > 서버 제품 선택시 서버 정보 입력 UI 확인
    @Test
    fun selectCustomServerProductUITest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)

        Espresso.onView(ViewMatchers.withId(R.id.productSelectLayout)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.customServerLayout)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.serverURLEditText)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    // 환경설정 > 서버 제품 선택시 서버 정보 입력시 입력 HTTP URL 확인
    @Test
    fun selectCustomServerProductHttpURLTest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)

        Espresso.onView(ViewMatchers.withId(R.id.productSelectLayout)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.customServerLayout)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.serverURLEditText)).perform(ViewActions.replaceText("www.rview.com"))
        Espresso.onView(ViewMatchers.withId(R.id.httpRadioButton)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.serverURLEditText)).check(ViewAssertions.matches(ViewMatchers.withText("http://www.rview.com")))
    }

    // 환경설정 > 서버 제품 선택시 서버 정보 입력시 입력 HTTPS URL 확인
    @Test
    fun selectCustomServerProductHttpsURLTest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)

        Espresso.onView(ViewMatchers.withId(R.id.productSelectLayout)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.customServerLayout)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.serverURLEditText)).perform(ViewActions.replaceText("www.rview.com"))
        Espresso.onView(ViewMatchers.withId(R.id.httpsRadioButton)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.serverURLEditText)).check(ViewAssertions.matches(ViewMatchers.withText("https://www.rview.com")))
    }


    // 환경설정 > 서버 제품 선택시 서버 정보 입력시 입력 http://, https 를 입력하지 않고 설정했을때 https 가 추가되는지를 확인한다.
    @Test
    fun selectCustomServerProductURLAppendHttpsTest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)

        Espresso.onView(ViewMatchers.withId(R.id.productSelectLayout)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.customServerLayout)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.httpsRadioButton)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.serverURLEditText)).perform(ViewActions.replaceText("www.rview.com"))

        Espresso.pressBack()
        val serverInfo = configRepository.getServerInfo()
        MatcherAssert.assertThat("서버주소에 https 설정이 안되서 실패", serverInfo.url, Matchers.`is`("https://www.rview.com"))
    }

    // 환경설정 > 서버 제품 선택시 서버 정보 입력시 입력 http://, https 를 입력하지 않고 설정했을때 http 가 추가되는지를 확인한다.
    @Test
    fun selectCustomServerProductURLAppendHttpTest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)

        Espresso.onView(ViewMatchers.withId(R.id.productSelectLayout)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.customServerLayout)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.httpRadioButton)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.serverURLEditText)).perform(ViewActions.replaceText("www.rview.com"))

        Espresso.pressBack()
        val serverInfo = configRepository.getServerInfo()
        MatcherAssert.assertThat("서버주소에 http 설정이 안되서 실패", serverInfo.url, Matchers.`is`("http://www.rview.com"))
    }

    // 환경설정 > 프록사 사용 ON 상태 visible 확인
    @Test
    fun selectProxyInfoVisibleTest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)

        swipeUp()

        Espresso.onView(ViewMatchers.withId(R.id.proxyToggleButton)).perform(ViewActions.click())

        swipeUp()

        Espresso.onView(ViewMatchers.withId(R.id.proxyAddrEditText)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        Espresso.onView(ViewMatchers.withId(R.id.proxyPortEditText)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        Espresso.onView(ViewMatchers.withId(R.id.proxyUserEditText)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
        Espresso.onView(ViewMatchers.withId(R.id.proxyPwdEditText)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    // 환경설정 > 프록사 사용 ON 상태 gone 확인
    @Test
    fun selectProxyInfoGoneTest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)

        swipeUp()

        Espresso.onView(ViewMatchers.withId(R.id.proxyToggleButton)).perform(ViewActions.click())

        swipeUp()

        Espresso.onView(ViewMatchers.withId(R.id.proxyToggleButton)).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.proxyAddrEditText)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        Espresso.onView(ViewMatchers.withId(R.id.proxyPortEditText)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        Espresso.onView(ViewMatchers.withId(R.id.proxyUserEditText)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
        Espresso.onView(ViewMatchers.withId(R.id.proxyPwdEditText)).check(ViewAssertions.matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    // 환경설정 > 프록사 사용 ON 상태서 정보 입력 데이터 확인
    @Test
    fun selectProxyInfoTest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)

        swipeUp()

        Espresso.onView(ViewMatchers.withId(R.id.proxyToggleButton)).perform(ViewActions.click())

        swipeUp()

        val proxyInfo = ProxyInfo("www.rview.com", "80", "tester", "111111")

        Espresso.onView(ViewMatchers.withId(R.id.proxyAddrEditText)).perform(ViewActions.replaceText(proxyInfo.address))
        Espresso.onView(ViewMatchers.withId(R.id.proxyPortEditText)).perform(ViewActions.replaceText(proxyInfo.port))
        Espresso.onView(ViewMatchers.withId(R.id.proxyUserEditText)).perform(ViewActions.replaceText(proxyInfo.id))
        Espresso.onView(ViewMatchers.withId(R.id.proxyPwdEditText)).perform(ViewActions.replaceText(proxyInfo.pwd))

        val savedProxyInfo = configRepository.getProxyInfo()
        MatcherAssert.assertThat("proxy 서버가 설정되지 않아서 실패", savedProxyInfo, Matchers.`is`(proxyInfo))
    }


    // 기타 > 의견보내기 - 공유 파업 노출 되는지 확인
    @Test
    fun startSharePopupTest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)
        swipeUp()

        Espresso.onView(ViewMatchers.withId(R.id.feedBackLayout)).perform(ViewActions.click())

        Intents.intended(hasAction(Intent.ACTION_SEND))
    }

    // 기타 > 웹사이트로 이동 - 브라우저로 웹사이트 실행 되는지 확인
    @Test
    fun startBrowserTest() = runBlocking<Unit> {
        ActivityScenario.launch(BasicSettingActivity::class.java)

        swipeUp()

        Espresso.onView(ViewMatchers.withId(R.id.goWebSiteLayout)).perform(ViewActions.click())
        Intents.intended(hasAction(Intent.ACTION_VIEW))
    }

    private fun swipeUp() {
        Espresso.onView(ViewMatchers.withId(R.id.layout_scrollview))
                .perform(ViewActions.swipeUp())
    }

    private fun hasAction(action: String): Matcher<Intent> {
        return object : TypeSafeMatcher<Intent>() {
            override fun describeTo(description: Description?) {
            }

            override fun matchesSafely(item: Intent?): Boolean {
                return item.toString().contains(action)
            }
        }
    }
}