package com.rsupport.mobile.agent.constant

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AgentBasicInfoTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun returnEmptyAccessTokenWhenNotSet() {
        val accessToken = AgentBasicInfo.getAccessToken(context)
        assertTrue(accessToken.isEmpty())
    }

    @Test
    fun returnAccessTokenWhenSet() {
        val inputAccessToken = "token_123"
        AgentBasicInfo.setAccessToken(context, inputAccessToken)
        val resultAccessToken = AgentBasicInfo.getAccessToken(context)
        assertEquals(inputAccessToken, resultAccessToken)
    }

    @Test
    fun returnEmptyRefreshTokenWhenNotSet() {
        val refreshToken = AgentBasicInfo.getRefreshToken(context)
        assertTrue(refreshToken.isEmpty())
    }

    @Test
    fun returnRefreshTokenWhenSet() {
        val inputRefreshToken = "token_123"
        AgentBasicInfo.setRefreshToken(context, inputRefreshToken)
        val resultAccessToken = AgentBasicInfo.getRefreshToken(context)
        assertEquals(inputRefreshToken, resultAccessToken)
    }

    @Test
    fun returnEmptyApiVersionWhenNotSet() {
        val apiVersion = AgentBasicInfo.getApiVersion(context)
        assertTrue(apiVersion.isEmpty())
    }

    @Test
    fun returnApiVersionWhenSet() {
        val insertApiVersion = "v2"
        AgentBasicInfo.setApiVersion(context, insertApiVersion)
        val resultApiVersion = AgentBasicInfo.getApiVersion(context)
        assertEquals(insertApiVersion, resultApiVersion)
    }


    @Test
    fun returnEmptyRefreshTokenURLWhenNotSet() {
        val refreshTokenURL = AgentBasicInfo.getRefreshTokenURL(context)
        assertTrue(refreshTokenURL.isEmpty())
    }

    @Test
    fun returnRefreshTokenURLWhenSet() {
        val insertRefreshTokenURL = "http://rv-server.rsup.io/refreshtoken"
        AgentBasicInfo.setRefreshTokenURL(context, insertRefreshTokenURL)
        val resultRefreshTokenURL = AgentBasicInfo.getRefreshTokenURL(context)
        assertEquals(insertRefreshTokenURL, resultRefreshTokenURL)
    }


}