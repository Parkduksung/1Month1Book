package com.rsupport.mobile.agent.ui.settings.delete

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.filters.LargeTest
import com.nhaarman.mockitokotlin2.any
import com.rsupport.mobile.agent.utils.SdkVersion
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.ui.agent.agentinfo.AgentInfoInteractor
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.rscommon.exception.RSException
import kotlinx.coroutines.runBlocking
import org.hamcrest.core.StringContains
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent.inject
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@LargeTest
@RunWith(MockitoJUnitRunner::class)
class AgentDeleteActivityMockTest {
    private val USER_ID = "UserId"
    private val USER_PWD = "UserPwd"
    private val UNDEFINED_ERROR_CODE = -999

    @Mock
    lateinit var agentDeleteInteractor: AgentDeleteInteractor

    @Mock
    lateinit var sdkVersion: SdkVersion


    @Before
    fun setup() = runBlocking<Unit> {
        loadKoinModules(listOf(
                module {
                    factory(override = true) { agentDeleteInteractor }
                    factory(override = true) { sdkVersion }
                }
        ))
    }

    @After
    fun tearDown() = runBlocking {
        loadKoinModules(listOf(
                module {
                    factory(override = true) { AgentDeleteInteractor() }
                    factory(override = true) { SdkVersion() }
                }
        ))
    }

    private fun launchAgentDeleteActivity() {
        ActivityScenario.launch(AgentDeleteActivity::class.java)
    }

    // 유효하지 않은 id 또는 pwd 를 입력하였을 경우를 확인한다.
    @Test
    fun invalidUserIdOrPwdTest() = runBlocking<Unit> {
        Mockito.`when`(agentDeleteInteractor.deleteAgent(any(), any())).thenReturn(Result.failure(RSException(ErrorCode.SETTING_INVALID_ACCOUNT_OR_PWD)))

        launchAgentDeleteActivity()

        setupUserId()
        setupUserPwd()
        performDeleteClick()

        Espresso.onView(ViewMatchers.withText(StringContains.containsString(ErrorCode.SETTING_INVALID_ACCOUNT_OR_PWD.toString())))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

    }

    // id 와 pwd 를 입력하지 않은 상태를 확인한다.
    @Test
    fun emptyUserIdAndPwdTest() = runBlocking<Unit> {
        launchAgentDeleteActivity()

        performDeleteClick()

        Espresso.onView(ViewMatchers.withText(R.string.msg_inputlogininfo))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }


    // id 만 입력하고 pwd 를 입력하지 않은 상태를 확인한다.
    @Test
    fun emptyUserPwdTest() = runBlocking<Unit> {
        launchAgentDeleteActivity()

        setupUserId()
        performDeleteClick()

        Espresso.onView(ViewMatchers.withText(R.string.msg_inputloginpwd))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

    }

    // pwd 만 입력하고 id 를 입력하지 않은 상태를 확인한다.
    @Test
    fun emptyUserIdTest() = runBlocking<Unit> {
        launchAgentDeleteActivity()

        setupUserPwd()
        performDeleteClick()

        Espresso.onView(ViewMatchers.withText(R.string.msg_inputloginid))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

    }

    // 삭제 성공했을 경우를 로그인 화면으로 이동을 확인한다.
    @Test
    fun deleteAgentSuccessTest() = runBlocking<Unit> {
        Mockito.`when`(agentDeleteInteractor.deleteAgent(any(), any())).thenAnswer {
            // 로그인 화면으로 이동후 로그인 버튼이 보이기 위한 조건으로 agent 가 삭제되어야하며 튜토리얼을 본 상태여야한다.
            runBlocking {
                val agentInfoInteractor by inject(AgentInfoInteractor::class.java)
                agentInfoInteractor.removeAgent()
                agentInfoInteractor.release()

                val configRepository by inject(ConfigRepository::class.java)
                configRepository.setShowTutorial(true)
            }
            Result.success(true)
        }

        launchAgentDeleteActivity()

        setupUserId()
        setupUserPwd()

        performDeleteClick()

        Espresso.onView(ViewMatchers.withId(R.id.loginButton))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }


    // 이미 삭제된 agent 에대해 삭제 요청을 했을경우를 확인한다.
    @Test
    fun alreadyDeletedAgentTest() = runBlocking<Unit> {
        Mockito.`when`(agentDeleteInteractor.deleteAgent(any(), any())).thenReturn(Result.failure(RSException(ErrorCode.SETTING_ALREADY_DELETE_AGENT)))

        launchAgentDeleteActivity()

        setupUserId()
        setupUserPwd()
        performDeleteClick()

        Espresso.onView(ViewMatchers.withText(StringContains.containsString(ErrorCode.SETTING_ALREADY_DELETE_AGENT.toString())))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))


    }

    // RSException 이 발생했으나 정의하지 않은 오류 발생시 오류 코드를 확인한다.
    @Test
    fun undefinedRSExceptionTest() = runBlocking<Unit> {
        Mockito.`when`(agentDeleteInteractor.deleteAgent(any(), any())).thenReturn(Result.failure(RSException(UNDEFINED_ERROR_CODE)))

        launchAgentDeleteActivity()

        setupUserId()
        setupUserPwd()

        performDeleteClick()

        Espresso.onView(ViewMatchers.withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(StringContains.containsString(UNDEFINED_ERROR_CODE.toString()))))
    }

    // 정의하지 않은 Exception 이 발생했을때 ErrorCode.UNKNOWN 을 확인한다.
    @Test
    fun undefinedExceptionTest() = runBlocking<Unit> {
        Mockito.`when`(agentDeleteInteractor.deleteAgent(any(), any())).thenReturn(Result.failure(RuntimeException("undefined exception")))

        launchAgentDeleteActivity()

        setupUserId()
        setupUserPwd()

        performDeleteClick()

        Espresso.onView(ViewMatchers.withId(R.id.tvcontent))
                .inRoot(isDialog())
                .check(ViewAssertions.matches(ViewMatchers.withText(StringContains.containsString(ErrorCode.UNKNOWN_ERROR.toString()))))
    }

    private fun performDeleteClick() {
        Espresso.onView(ViewMatchers.withId(R.id.btn_delete)).perform(click())
    }

    private fun setupUserPwd() {
        Espresso.onView(ViewMatchers.withId(R.id.userpasswd)).perform(ViewActions.typeText(USER_PWD))
        Espresso.closeSoftKeyboard()
    }

    private fun setupUserId() {
        Espresso.onView(ViewMatchers.withId(R.id.userid)).perform(ViewActions.typeText(USER_ID))
        Espresso.closeSoftKeyboard()
    }
}