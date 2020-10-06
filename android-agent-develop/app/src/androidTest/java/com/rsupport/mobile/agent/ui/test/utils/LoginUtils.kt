package com.rsupport.mobile.agent.ui.test.utils

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import com.rsupport.mobile.agent.utils.SdkVersion
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.ui.agent.agentinfo.AgentInfoInteractor
import kotlinx.coroutines.delay
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent
import org.mockito.Mockito
import com.rsupport.mobile.agent.ui.launcher.LauncherActivity

class LoginUtils {
    companion object {
        private const val COLLECT_CORP_ID = "kwcho"
        private const val COLLECT_ID = "kwcho"
        private const val COLLECT_PWD = "111111"
    }

    private val afterDelayTimeMs = 1L
    private lateinit var sdkVersion: SdkVersion


    // login
    suspend fun performLogin() {
        performViewCorrectLoginInfo()
        performLoginClick()
    }

    suspend fun setup() {
        setupSdkVersion()

        KoinJavaComponent.inject(AgentInfoInteractor::class.java).value.apply {
            removeAgent()
        }

        KoinJavaComponent.inject(ConfigRepository::class.java).apply {
            value.setShowTutorial(true)
        }

        Mockito.`when`(sdkVersion.greaterThan23()).thenReturn(false)
    }

    suspend fun launch() {
        ActivityScenario.launch(LauncherActivity::class.java)
    }

    suspend fun tearDown() {
        delay(afterDelayTimeMs)
        unloadKoinModules(module {
            factory { SdkVersion() }
        })

        loadKoinModules(module {
            factory { SdkVersion() }
        })
    }

    /**
     * LoginActivity 상태인걸 확인한다.
     */
    fun verifyLoginActivity() {
        Espresso.onView(ViewMatchers.withId(R.id.corpid)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    private fun setupSdkVersion() {
        sdkVersion = Mockito.spy(SdkVersion())

        unloadKoinModules(module {
            factory { SdkVersion() }
        })

        loadKoinModules(module {
            factory { sdkVersion }
        })
    }

    private fun performLoginClick() {
        Espresso.onView(ViewMatchers.withId(R.id.loginButton)).perform(ViewActions.click())
    }

    private fun performViewCorrectLoginInfo() {
        verifyLoginActivity()

        Espresso.onView(ViewMatchers.withId(R.id.corpid)).perform(ViewActions.typeText(COLLECT_CORP_ID))
        Espresso.closeSoftKeyboard()
        Espresso.onView(ViewMatchers.withId(R.id.userid)).perform(ViewActions.typeText(COLLECT_ID))
        Espresso.closeSoftKeyboard()
        Espresso.onView(ViewMatchers.withId(R.id.userpasswd)).perform(ViewActions.typeText(COLLECT_PWD))
        Espresso.closeSoftKeyboard()
    }


}