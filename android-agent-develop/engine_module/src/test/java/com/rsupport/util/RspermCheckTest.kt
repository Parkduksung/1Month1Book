package com.rsupport.util

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.rsupport.util.validator.LG318RSpermPackageValidator
import com.rsupport.util.validator.PackageValidator
import junit.framework.Assert.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.Test

class RspermCheckTest {
    @Test
    fun get_package_name_when_installed() {
        val packageManager: PackageManager = mock()
        val packageValidator: PackageValidator = mock()

        given(packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES)).willAnswer {
            listOf(PackageInfo().apply {
                packageName = "com.rsupport.rsperm"
                versionName = "Build 100"
            })
        }

        given(packageValidator.isValidate(any())).willAnswer { true }


        val rspermCheck = RspermCheck(packageManager, packageValidator)

        val rspermPackageName = rspermCheck.installed("com.rsupport.rsperm")
        assertNotNull(rspermPackageName)
    }

    @Test
    fun not_found_package_name_when_installed_318_lg() {
        val packageManager: PackageManager = mock()

        given(packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES)).willAnswer {
            listOf(PackageInfo().apply {
                packageName = "com.rsupport.rsperm"
                versionName = "Build 318"
            })
        }
        val rspermCheck = RspermCheck(packageManager, LG318RSpermPackageValidator("lg"))
        val rspermPackageName = rspermCheck.installed("com.rsupport.rsperm")

        assertNull(rspermPackageName)
    }

    @Test
    fun sorted_default_rsperm_pkg_name() {
        val packageManager: PackageManager = mock()

        given(packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES)).willAnswer {
            listOf(
                    PackageInfo().apply {
                        packageName = "com.rsupport.rsperm.ay.a1"
                        versionName = "Build 100"
                    },
                    PackageInfo().apply {
                        packageName = "com.rsupport.rsperm"
                        versionName = "Build 100"
                    },
                    PackageInfo().apply {
                        packageName = "com.rsupport.rsperm.ay.a1th"
                        versionName = "Build 100"
                    }
            )
        }
        val rspermCheck = RspermCheck(packageManager, LG318RSpermPackageValidator("lg"))
        val rspermPackageName = rspermCheck.installed("com.rsupport.rsperm")

        assertThat(rspermPackageName, `is`("com.rsupport.rsperm.ay.a1th"))
    }
}


