package com.rsupport.mobile.agent.modules.sysinfo

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import com.rsupport.mobile.agent.modules.sysinfo.appinfo.AppInfoHolder
import com.rsupport.mobile.agent.utils.Utility
import com.rsupport.util.log.RLog

class ApplicationInfoHelper(private val context: Context) {
    fun getLabelName(packagename: String?): String {
        var label = ""
        try {
            label = context.getPackageManager().getApplicationInfo(packagename, 0).loadLabel(context.getPackageManager()).toString()
        } catch (e: Exception) {
            RLog.v(e)
        }
        return label
    }


    @Throws(PackageManager.NameNotFoundException::class)
    fun getApplicationInfo(appItem: String?): ApplicationInfo? {
        val pm: PackageManager = context.getPackageManager()
        val allApps = pm.getInstalledApplications(0)
        var findPackageName = appItem
        for (applicationInfo in allApps) {
            if (applicationInfo.packageName == appItem || applicationInfo.loadLabel(pm) == appItem) {
                findPackageName = applicationInfo.packageName
            }
        }
        return pm.getApplicationInfo(findPackageName, 0)
    }

    fun getLaunchIntent(appInfoHolder: AppInfoHolder): Intent? {
        return context.packageManager.getLaunchIntentForPackage(appInfoHolder.appInfo.packageName)
    }

    @Throws(ActivityNotFoundException::class)
    fun startActivity(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or
                Intent.FLAG_ACTIVITY_FORWARD_RESULT or
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP or
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        context.startActivity(intent)
    }

    fun startContactActivity(packageName: String): Boolean {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("content://contacts/people")))
        } catch (e2: ActivityNotFoundException) {
            RLog.e("$packageName does not start.")
            return false
        }
        return true
    }

    fun isWifiConnected(): Boolean {
        return Utility.isWifiReady()
    }

    fun getInstalledApplications(): List<ApplicationInfo?>? {
        return context.packageManager.getInstalledApplications(0)
    }

    @Throws(PackageManager.NameNotFoundException::class)
    fun getPackageInfo(info: ApplicationInfo): PackageInfo? {
        return context.packageManager.getPackageInfo(info.packageName, 0)
    }


}