package com.rsupport.mobile.agent.extension

import android.content.Context
import android.content.pm.PackageManager

fun String.isInstalledPkg(context: Context): Boolean {
    return context.packageManager.getInstalledPackages(PackageManager.GET_SERVICES).map { it.packageName }.any {
        it == this
    }
}