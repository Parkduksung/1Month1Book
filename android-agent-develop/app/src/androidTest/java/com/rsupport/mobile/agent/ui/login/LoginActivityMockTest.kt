package com.rsupport.mobile.agent.ui.login

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.filters.LargeTest
import com.nhaarman.mockitokotlin2.any
import com.rsupport.mobile.agent.utils.SdkVersion
import com.rsupport.mobile.agent.modules.engine.EngineType
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.api.model.ConsoleLoginResult
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.ui.agent.agentinfo.AgentInfoInteractor
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rscommon.exception.RSException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.hamcrest.core.StringContains.containsString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
@LargeTest
class LoginActivityMockTest {
    private val afterDelayTimeMs = 1L
    private val serverURL = "http://rview.com"

    @Mock
    private lateinit var loginInteractor: LoginInteractor

    @Mock
    private lateinit var agentInfoInteractor: AgentInfoInteractor

    private lateinit var sdkVersion: SdkVersion

    private val configRepository: ConfigRepository by inject(ConfigRepository::class.java)

    private val context: Context by inject(Context::class.java)

    companion object {
        private const val COLLECT_CORP_ID = "kwcho"
        private const val COLLECT_ID = "kwcho"
        private const val COLLECT_PWD = "111111"

        private const val INCOLLECT_PWD = "222222"
    }

    @Before
    fun setup() = runBlocking<Unit> {
        setupInteractor()

        configRepository.setFirstLaunch(false)

        Mockito.`when`(sdkVersion.greaterThan23()).thenReturn(false)
        Mockito.`when`(agentInfoInteractor.isAgentInstalled()).thenReturn(false)
    }


    private suspend fun setupInteractor() = withContext(Dispatchers.IO) {
        unloadKoinModules(module {
            factory { SdkVersion() }
        })

        sdkVersion = Mockito.spy(SdkVersion())
        loadKoinModules(module {
            factory { sdkVersion }
        })

        unloadKoinModules(module {
            factory { LoginInteractor() }
            factory { AgentInfoInteractor() }
        })

        loadKoinModules(module {
            factory { loginInteractor }
            factory { agentInfoInteractor }
        })

        agentInfoInteractor.removeAgent()
    }


    @After
    fun tearDown() = runBlocking {
        delay(afterDelayTimeMs)

        unloadKoinModules(module {
            factory { sdkVersion }
            factory { loginInteractor }
            factory { agentInfoInteractor }
        })

        loadKoinModules(module {
            factory { SdkVersion() }
            factory { LoginInteractor() }
            factory { AgentInfoInteractor() }
        })
    }

    private suspend fun launchLoginActivity() {
        ActivityScenario.launch(LoginActivity::class.java)
    }

    // 앱 처음 실행시 튜토리얼로 이동하는지 확인
    @Test
    fun launchTutorialTest() = runBlocking<Unit> {
        Mockito.`when`(loginInteractor.isShowTutorial()).thenReturn(false)

        launchLoginActivity()

        Espresso.onView(withId(R.id.gallery_layer)).check(ViewAssertions.matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
    }

    // 잘못된 서버 URL 로 설정되었을때 오류를 확인한다.
    @Test
    fun invalidURLTest() = runBlocking<Unit> {
        setupShowTutorial()
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(false)

        launchLoginActivity()

        performViewCorrectLoginInfo()
        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(containsString(ErrorCode.LOGIN_INVALID_URL.toString()))))
    }

    // 정상 로그인시 Agent Install 로 이동
    @Test
    fun loginAgentInstallTest() = runBlocking<Unit> {
        setupShowTutorial()
        setupSupportEngineRSPerm()
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.consoleLogin(any(), any(), any())).thenReturn(Result.success(ConsoleLoginState.from(ConsoleLoginResult())))

        launchLoginActivity()

        performViewCorrectLoginInfo()
        performLoginClick()

        // check agentInstallActivity
        Espresso.onView(withId(R.id.agent_install_des)).check(ViewAssertions.matches(withId(R.id.agent_install_des)))
    }

    // 잘못된 로그인 정보시 오류 확인
    @Test
    fun loginPwdFailTest() = runBlocking<Unit> {
        setupShowTutorial()
        setupSupportEngineRSPerm()
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.consoleLogin(any(), any(), any())).thenReturn(Result.failure(RSException(ErrorCode.LOGIN_INVALID_USER_ACCOUNT_OR_PWD)))

        launchLoginActivity()

        performViewInCorrectLoginInfo()
        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(containsString(ErrorCode.LOGIN_INVALID_USER_ACCOUNT_OR_PWD.toString()))))
    }

    // 아무것도 입력하지 않음.
    @Test
    fun loginEmptyFailTest() = runBlocking<Unit> {
        setupShowTutorial()
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)

        launchLoginActivity()

        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(containsString(ErrorCode.LOGIN_EMPTY_ID.toString()))))
    }

    // 아이디를 입력하지 않음
    @Test
    fun loginEmptyIdFailTest() = runBlocking<Unit> {
        setupShowTutorial()
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)

        launchLoginActivity()

        Espresso.onView(withId(R.id.corpid)).perform(typeText(COLLECT_CORP_ID))
        Espresso.onView(withId(R.id.userpasswd)).perform(typeText(COLLECT_PWD))
        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(containsString(ErrorCode.LOGIN_EMPTY_ID.toString()))))
    }

    // 비밀번호를 입력하지 않음.
    @Test
    fun loginEmptyPwdFailTest() = runBlocking<Unit> {
        setupShowTutorial()
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        launchLoginActivity()

        Espresso.onView(withId(R.id.corpid)).perform(typeText(COLLECT_CORP_ID))
        Espresso.closeSoftKeyboard()
        Espresso.onView(withId(R.id.userid)).perform(typeText(COLLECT_ID))
        Espresso.closeSoftKeyboard()
        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(containsString(ErrorCode.LOGIN_EMPTY_PWD.toString()))))
    }


    // 정의되지 않은 예외 발생시 지원하지 않는 Engine 테스트
    @Test
    fun engineUndefinedExceptionNotSupportTest() = runBlocking<Unit> {
        setupShowTutorial()
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.failure(RuntimeException("")))

        launchLoginActivity()

        performViewCorrectLoginInfo()
        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(containsString(ErrorCode.ENGINE_NOT_SUPPORTED.toString()))))
    }

    // 단말 업데이트 필요시 확인 테스트
    @Test
    fun engineNeedOSUpdateTest() = runBlocking<Unit> {
        setupShowTutorial()
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.failure(RSException(ErrorCode.ENGINE_KNOX_BLACK_LIST_WEB_854)))

        launchLoginActivity()

        performViewCorrectLoginInfo()
        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(containsString(ErrorCode.ENGINE_KNOX_BLACK_LIST_WEB_854.toString()))))
    }

    // Black list 단말 확인
    @Test
    fun engineNotSupportTest() = runBlocking<Unit> {
        setupShowTutorial()
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.failure(RSException(ErrorCode.ENGINE_KNOX_BLACK_LIST_WEB_855)))

        launchLoginActivity()

        performViewCorrectLoginInfo()
        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(containsString(ErrorCode.ENGINE_KNOX_BLACK_LIST_WEB_855.toString()))))
    }

    // 파라메터 부족이나 잘못된 정보시 지원하지 않는 단말 테스트 - Knox Black list 조회 오류
    @Test
    fun engineUndefinedErrorCodeTest() = runBlocking<Unit> {
        setupShowTutorial()
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.failure(RSException(ErrorCode.ENGINE_KNOX_BLACK_LIST_WEB_111)))

        launchLoginActivity()

        performViewCorrectLoginInfo()
        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(containsString(ErrorCode.ENGINE_KNOX_BLACK_LIST_WEB_111.toString()))))
    }


    // 비밀번호가 만료되었을때 오류코드 확인
    @Test
    fun expiredPwdTest() = runBlocking<Unit> {
        setupShowTutorial()
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_RSPERM)))
        Mockito.`when`(loginInteractor.consoleLogin(any(), any(), any())).thenReturn(Result.failure(RSException(ErrorCode.LOGIN_EXPIRED_PWD)))

        launchLoginActivity()

        performViewCorrectLoginInfo()
        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(containsString(ErrorCode.LOGIN_EXPIRED_PWD.toString()))))
    }

    // [AES 암호화]등록되지 않은 ID
    @Test
    fun aesUserIdErrorTest() = runBlocking<Unit> {
        setupShowTutorial()
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_RSPERM)))
        Mockito.`when`(loginInteractor.consoleLogin(any(), any(), any())).thenReturn(Result.failure(RSException(ErrorCode.LOGIN_INVALID_ID_AES)))

        launchLoginActivity()

        performViewCorrectLoginInfo()
        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(containsString(ErrorCode.LOGIN_INVALID_ID_AES.toString()))))
    }

    // [AES 암호화]잘못된 Agent ID 또는 패스워드
    @Test
    fun aesUserIdOrPwdErrorTest() = runBlocking<Unit> {
        setupShowTutorial()
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_RSPERM)))
        Mockito.`when`(loginInteractor.consoleLogin(any(), any(), any())).thenReturn(Result.failure(RSException(ErrorCode.LOGIN_INVALID_USER_ACCOUNT_OR_PWD)))

        launchLoginActivity()

        performViewCorrectLoginInfo()
        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(containsString(ErrorCode.LOGIN_INVALID_USER_ACCOUNT_OR_PWD.toString()))))
    }

    // 강제 업데이트 오류 코드 발생시 팝업확인.
    @Test
    fun forceUpdateTest() = runBlocking<Unit> {
        setupShowTutorial()

        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_RSPERM)))
        Mockito.`when`(loginInteractor.consoleLogin(any(), any(), any())).thenReturn(Result.failure(RSException(ErrorCode.LOGIN_FORCE_UPDATE)))

        launchLoginActivity()

        performViewCorrectLoginInfo()
        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(context.getString(R.string.weberr_update_force))))
    }

    // 멤버 업그레이드 팝업 확인
    @Test
    fun upgradeMemberTest() = runBlocking<Unit> {
        setupShowTutorial()

        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_RSPERM)))
        Mockito.`when`(loginInteractor.consoleLogin(any(), any(), any())).thenReturn(Result.failure(RSException(ErrorCode.LOGIN_UPGRADE_MEMBER)))

        launchLoginActivity()

        performViewCorrectLoginInfo()
        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(context.getString(R.string.weberr_need_update_member))))
    }


    // Proxy 설정 오류 LOGIN_NET_ERR_PROXY_VERIFY
    @Test
    fun proxyErrorTest() = runBlocking<Unit> {
        setupShowTutorial()

        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_RSPERM)))
        Mockito.`when`(loginInteractor.consoleLogin(any(), any(), any())).thenReturn(Result.failure(RSException(ErrorCode.LOGIN_NET_ERR_PROXY_VERIFY)))

        launchLoginActivity()

        performViewCorrectLoginInfo()
        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(containsString(ErrorCode.LOGIN_NET_ERR_PROXY_VERIFY.toString()))))
    }

    // App update 유효한 상태
    @Test
    fun appUpdateTest() = runBlocking<Unit> {
        setupShowTutorial()

        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_RSPERM)))
        Mockito.`when`(loginInteractor.consoleLogin(any(), any(), any())).thenReturn(Result.success(ConsoleLoginState.UpdateAvailable))

        launchLoginActivity()

        performViewCorrectLoginInfo()
        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(containsString(context.getString(R.string.weberr_update_recommendation)))))
    }

    // Passowrd 만료기간이 남았을 경우
    @Test
    fun passwordExpireDayTest() = runBlocking<Unit> {
        setupShowTutorial()

        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_RSPERM)))
        Mockito.`when`(loginInteractor.consoleLogin(any(), any(), any())).thenReturn(Result.success(ConsoleLoginState.PasswordExpireDay("1", serverURL)))

        launchLoginActivity()

        performViewCorrectLoginInfo()
        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(containsString(serverURL))))
    }


    // Rsperm 이 설치 가능할때 팝업이 노출되는지 확인한다.
    @Test
    fun rspermEngineDownloadMarketTest() = runBlocking<Unit> {
        setupShowTutorial()
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.failure(RSException(ErrorCode.ENGINE_NOT_SUPPORTED)))
        Mockito.`when`(loginInteractor.findInstallableEngine()).thenReturn("com.rsupport.rsperm.ay.a1th")
        Mockito.`when`(loginInteractor.consoleLogin(any(), any(), any())).thenReturn(Result.success(ConsoleLoginState.from(ConsoleLoginResult())))

        launchLoginActivity()

        performViewCorrectLoginInfo()
        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(containsString("RSPERM"))))
    }

    // 설치 가능한 Rsperm 이 없을때 오류 확인
    @Test
    fun rspermEngineNotFoundTest() = runBlocking<Unit> {
        setupShowTutorial()
        Mockito.`when`(loginInteractor.checkServerURL()).thenReturn(true)
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.failure(RSException(ErrorCode.ENGINE_NOT_SUPPORTED)))
        Mockito.`when`(loginInteractor.findInstallableEngine()).thenReturn("")
        Mockito.`when`(loginInteractor.consoleLogin(any(), any(), any())).thenReturn(Result.success(ConsoleLoginState.from(ConsoleLoginResult())))

        launchLoginActivity()

        performViewCorrectLoginInfo()
        performLoginClick()

        Espresso.onView(withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(containsString(ErrorCode.ENGINE_NOT_SUPPORTED.toString()))))
    }


    private fun performLoginClick() {
        Espresso.onView(withId(R.id.loginButton)).perform(click())
    }

    private fun setupShowTutorial() {
        Mockito.`when`(loginInteractor.isShowTutorial()).thenReturn(true)
    }

    private suspend fun setupSupportEngineRSPerm() {
        Mockito.`when`(loginInteractor.isSupportEngine()).thenReturn(Result.success(SupportEngine.from(EngineType.ENGINE_TYPE_RSPERM)))
    }

    private fun performViewInCorrectLoginInfo() {
        Espresso.onView(withId(R.id.corpid)).perform(typeText(COLLECT_CORP_ID))
        Espresso.onView(withId(R.id.userid)).perform(typeText(COLLECT_ID))
        Espresso.onView(withId(R.id.userpasswd)).perform(typeText(INCOLLECT_PWD))
    }

    private fun performViewCorrectLoginInfo() {
        Espresso.onView(withId(R.id.corpid)).perform(typeText(COLLECT_CORP_ID))
        Espresso.onView(withId(R.id.userid)).perform(typeText(COLLECT_ID))
        Espresso.onView(withId(R.id.userpasswd)).perform(typeText(COLLECT_PWD))
    }

}