package com.rsupport.mobile.agent.modules.engine

import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test

class KnoxKeyUpdateTest {

    // knox key 가 서버에서 정상적으로 받아지는지 확인한다.
    @Test
    fun requestKnoxLicenseKeyTest() = runBlocking<Unit> {
        val knoxKeyUpdate = KnoxKeyUpdate()
        val knoxKey = knoxKeyUpdate.update()
        MatcherAssert.assertThat("knox key 가 없어서 실패", knoxKey, Matchers.notNullValue())
    }
}