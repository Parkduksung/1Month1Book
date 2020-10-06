package com.rsupport.util.validator

import android.content.pm.PackageInfo
import android.os.Build

class LG318RSpermPackageValidator(private val manufacturer: String = Build.MANUFACTURER.toLowerCase()) : PackageValidator {
    override fun isValidate(packageInfo: PackageInfo): Boolean {
        return !isLG318PSperm(packageInfo.versionName, packageInfo.packageName)
    }

    // LG rsperm 중 aidl 순서가 변경되어 Build 318 버전은 사용할 수 없음.
    private fun isLG318PSperm(versionName: String, appName: String): Boolean {
        return (versionName.contains("Build 318")
                && manufacturer.contains("lg")
                && "com.rsupport.rsperm" == appName)
    }
}