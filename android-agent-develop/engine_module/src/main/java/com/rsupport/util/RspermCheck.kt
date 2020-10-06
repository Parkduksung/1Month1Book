package com.rsupport.util

import android.content.pm.PackageManager
import com.rsupport.util.validator.PackageValidator

class RspermCheck(private val packageManager: PackageManager, private val packageValidator: PackageValidator) {

    fun installed(pkgs: String): String? {
        val packageInfo = packageManager.getInstalledPackages(PackageManager.GET_SERVICES).apply {
            sortWith(
                    Comparator { o1, o2 ->
                        o2.packageName.length.compareTo(o1.packageName.length)
                    }
            )
        }

        packageInfo
                .filter { it.packageName.startsWith(pkgs) }
                .forEach {
                    if (packageValidator.isValidate(it)) {
                        return it.packageName
                    }
                }


        return null
    }
}