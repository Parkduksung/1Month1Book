package com.rsupport.mobile.agent.ui.login

import base.BaseTest
import com.rsupport.mobile.agent.constant.ComConstant
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import com.rsupport.android.engine.install.IEngineContext
import com.rsupport.android.engine.install.exception.IErrorCode
import com.rsupport.android.engine.install.gson.dto.EngineGSon
import com.rsupport.mobile.agent.api.ApiService
import com.rsupport.mobile.agent.api.model.ConsoleLoginResult
import com.rsupport.mobile.agent.error.ErrorCode
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.repo.config.ServerInfo
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.utils.SdkVersion
import com.rsupport.rscommon.exception.RSException
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.module.Module
import org.koin.dsl.module
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class LoginInteractorTest : BaseTest() {
    private val corpID = "corpId"
    private val userId = "userId"
    private val userPwd = "userPwd"

    private val loginInteractor = LoginInteractor()

    @Mock
    lateinit var configRepository: ConfigRepository

    @Mock
    lateinit var apiService: ApiService

    @Mock
    lateinit var engineContext: IEngineContext

    @Mock
    lateinit var sdkVersion: SdkVersion

    @Mock
    lateinit var engineChecker: EngineTypeCheck

    private val nodataEngineGson = EngineGSon().apply {
        returnCode = IErrorCode.WEB_RESULT_CODE_ERROR_NONE_DATA
        installFiles = arrayListOf()
    }

    private val installableEngineGson = EngineGSon().apply {
        returnCode = EngineGSon.OK
        installFiles = arrayListOf<EngineGSon.InstallFileInfo>().apply {
            add(
                    EngineGSon.InstallFileInfo().apply {
                        packageName = "rsperm.test.pkg"
                    }
            )
        }
    }

    override fun createModules(): List<Module> {
        return listOf(
                module {
                    single { configRepository }
                    single<ApiService> { apiService }
                    factory<IEngineContext> { engineContext }
                    single { sdkVersion }
                    single { engineChecker }
                }
        )
    }

    // tutorial 을 봣는지 확인한다.(봤을때)
    @Test
    fun checkTutorialTest() = runBlocking {
        Mockito.`when`(configRepository.isShowTutorial()).thenReturn(true)
        val isShowTutorialResult = loginInteractor.isShowTutorial()
        MatcherAssert.assertThat("튜토리얼을 보지 않아서 실패", isShowTutorialResult, Matchers.`is`(true))
    }

    // tutorial 을 봣는지 확인한다.(보지 않았을때)
    @Test
    fun checkTutorialFailTest() = runBlocking {
        Mockito.`when`(configRepository.isShowTutorial()).thenReturn(false)
        val isShowTutorialResult = loginInteractor.isShowTutorial()
        MatcherAssert.assertThat("튜토리얼을 봐서 않아서 실패", isShowTutorialResult, Matchers.`is`(false))
    }

    // 로그인 시도시 비밀번호 유효기간이 남아있을 경우
    @Test
    fun passwordExpireDayTest() = runBlocking {
        Mockito.`when`(apiService.consoleLogin(any(), any(), any(), any())).thenReturn(
                Result.success(
                        ConsoleLoginResult().apply {
                            passwordLimitDays = "1"
                        }
                )
        )
        val loginResult = loginInteractor.consoleLogin(corpID, userId, userPwd)
        val expireDay = ((loginResult as? Result.Success)?.value as? ConsoleLoginState.PasswordExpireDay)?.expireDay
        MatcherAssert.assertThat("남아있는 비밀번호 유효기간이 달라서 실패", expireDay, Matchers.`is`("1"))
    }


    // 로그인 시도시 새로운 버전의 앱이 있을 경우
    @Test
    fun newVersionAppTest() = runBlocking {
        Mockito.`when`(apiService.consoleLogin(any(), any(), any(), any())).thenReturn(
                Result.success(
                        ConsoleLoginResult().apply {
                            newVersion = "1"
                        }
                )
        )
        val loginResult = loginInteractor.consoleLogin(corpID, userId, userPwd)
        val updateVersion = (loginResult as? Result.Success)?.value as? ConsoleLoginState.UpdateAvailable
        MatcherAssert.assertThat("새로운 버전이 있는데 확인할 수 없어서 실패", updateVersion, Matchers.instanceOf(ConsoleLoginState.UpdateAvailable::class.java))
    }

    // 비밀번호 사용기간 만료 오류코드 변환 확인
    @Test
    fun passwordExpiredTest() = runBlocking<Unit> {
        Mockito.`when`(apiService.consoleLogin(any(), any(), any(), any())).thenReturn(Result.Failure(RSException(ComConstant.WEB_ERR_PASSWORD_EXPIRED.toInt())))

        val loginResult = loginInteractor.consoleLogin(corpID, userId, userPwd)
        val resultCode = ((loginResult as? Result.Failure)?.throwable as? RSException)?.errorCode
        MatcherAssert.assertThat("비밀번호 유효기간이 만료 오류코드가 달라서 실패", resultCode, Matchers.`is`(ErrorCode.LOGIN_EXPIRED_PWD.toInt()))
    }

    // AES 오류로 인한 아이디를 못찾았을때 오류코드 변환 확인
    @Test
    fun notFoundUserIdAesTest() = runBlocking<Unit> {
        Mockito.`when`(apiService.consoleLogin(any(), any(), any(), any())).thenReturn(Result.Failure(RSException(ComConstant.WEB_ERR_AES_NOT_FOUND_USERID.toInt())))

        val loginResult = loginInteractor.consoleLogin(corpID, userId, userPwd)
        val resultCode = ((loginResult as? Result.Failure)?.throwable as? RSException)?.errorCode
        MatcherAssert.assertThat("AES 암호화에 의한 ID 를 못찾아서 발생하는 오류코드가 달라서 실패", resultCode, Matchers.`is`(ErrorCode.LOGIN_INVALID_ID_AES.toInt()))
    }


    // AES 오류로 인한 아이디를 오류에 대한 오류코드 변환 확인
    @Test
    fun invalidUserIdAesTest() = runBlocking<Unit> {
        Mockito.`when`(apiService.consoleLogin(any(), any(), any(), any())).thenReturn(Result.Failure(RSException(ComConstant.WEB_ERR_AES_INVALID_USER_ACCOUNT.toInt())))

        val loginResult = loginInteractor.consoleLogin(corpID, userId, userPwd)
        val resultCode = ((loginResult as? Result.Failure)?.throwable as? RSException)?.errorCode
        MatcherAssert.assertThat("AES 암호화에 의한 ID가 유효하지 않아서 발생한 오류코드가 달라서 실패", resultCode, Matchers.`is`(ErrorCode.LOGIN_INVALID_USER_ACCOUNT_OR_PWD.toInt()))
    }

    // AES 오류로 인한 아이디를 오류에 대한 오류코드 변환 확인
    @Test
    fun invalidUserIdTest() = runBlocking<Unit> {
        Mockito.`when`(apiService.consoleLogin(any(), any(), any(), any())).thenReturn(Result.Failure(RSException(ComConstant.WEB_ERR_INVALID_USER_ACCOUNT.toInt())))

        val loginResult = loginInteractor.consoleLogin(corpID, userId, userPwd)
        val resultCode = ((loginResult as? Result.Failure)?.throwable as? RSException)?.errorCode
        MatcherAssert.assertThat("ID가 유효하지 않아서 발생한 오류코드가 달라서 실패", resultCode, Matchers.`is`(ErrorCode.LOGIN_INVALID_USER_ACCOUNT_OR_PWD.toInt()))
    }

    // 서버 정보가 정상적으로 설정 되어있는지를 확인한다.
    @Test
    fun checkServerInfoSuccessTest() = runBlocking {
        val serverInfo = ServerInfo("www.rview.com")
        Mockito.`when`(configRepository.getServerInfo()).thenReturn(serverInfo)

        val result = loginInteractor.checkServerURL()
        MatcherAssert.assertThat("서버 설정이 잘못되어서 실패", result, Matchers.`is`(true))
    }

    // 서버 정보가 잘못 설정되어 있을때 결과를 확인한다.
    @Test
    fun checkServerInfoFailTest() = runBlocking {
        val serverInfo = ServerInfo("")
        Mockito.`when`(configRepository.getServerInfo()).thenReturn(serverInfo)
        val result = loginInteractor.checkServerURL()
        MatcherAssert.assertThat("서버 설정이 잘못되어서 실패", result, Matchers.`is`(false))
    }


    // engine 이 rsperm 일때 사용가능한 rsperm 이 있으면 rsperm
    @Test
    fun supportRspermEngine() = runBlocking<Unit> {
        whenever(engineContext.requestFindRsperm()).thenReturn(installableEngineGson)

        val result = loginInteractor.isSupportEngine()
        MatcherAssert.assertThat(result, Matchers.instanceOf(Result.Success::class.java))
    }

    // engine 이 rsperm 일때 사용가능한 rsperm 이 없고 설치 가능한 rsperm 이 없고 sdk 가 21 이상일때는 rsperm
    @Test
    fun supportRspermWhenNotFoundRspermButSdk21Over() = runBlocking<Unit> {
        whenever(engineContext.requestFindRsperm()).thenReturn(nodataEngineGson)
        whenever(engineContext.requestInstallableRsperm()).thenReturn(nodataEngineGson)

        whenever(sdkVersion.greaterThan21()).thenReturn(true)

        val result = loginInteractor.isSupportEngine()
        MatcherAssert.assertThat(result, Matchers.instanceOf(Result.Success::class.java))
    }

    // engine 이 rsperm 일때 사용가능한 rsperm 이 없고 설치 가능한 rsperm 이 있으면 지원하지 않음.
    @Test
    fun notsupportRspermWhenInstallable() = runBlocking<Unit> {
        whenever(engineContext.requestFindRsperm()).thenReturn(nodataEngineGson)

        whenever(engineContext.requestInstallableRsperm()).thenReturn(installableEngineGson)

        val result = loginInteractor.isSupportEngine()
        MatcherAssert.assertThat(result, Matchers.instanceOf(Result.Failure::class.java))
    }

}