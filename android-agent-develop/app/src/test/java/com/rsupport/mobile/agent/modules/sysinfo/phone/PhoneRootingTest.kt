package com.rsupport.mobile.agent.modules.sysinfo.phone

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class PhoneRootingTest {

    @Test
    fun phone() = runBlocking<Unit> {
        val phoneRooting = PhoneRooting()

        val isRooting = phoneRooting.isRooting()
        assertFalse(isRooting)
    }
}
