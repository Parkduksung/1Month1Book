package com.rsupport.mobile.agent.extension

import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StringTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext


    // 설치되어 있지 않은 pkg 를 확인한다.
    @Test
    fun notExistPackageTest() {
        val pkgName = "com.not.exist.package"
        MatcherAssert.assertThat("설치되어 있지 않은 pkg 가 설치되어 있다고 해서 실패.", pkgName.isInstalledPkg(context), Matchers.`is`(false))
    }

    // 설치되어있는 pkg 를 확인한다.
    @Ignore
    @Test
    fun existPackagesTest() {
        context.packageManager.getInstalledPackages(PackageManager.GET_SERVICES).map { it.packageName }.forEach {
            MatcherAssert.assertThat("설치된 pkg가 설치안되어있다고해서 실패.", it.isInstalledPkg(context), Matchers.`is`(true))
        }
    }
}