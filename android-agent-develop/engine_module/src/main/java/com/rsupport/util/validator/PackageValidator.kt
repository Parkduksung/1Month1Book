package com.rsupport.util.validator

import android.content.pm.PackageInfo

interface PackageValidator {
    fun isValidate(packageInfo: PackageInfo): Boolean
}