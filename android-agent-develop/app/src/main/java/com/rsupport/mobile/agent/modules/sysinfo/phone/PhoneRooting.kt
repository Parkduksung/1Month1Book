package com.rsupport.mobile.agent.modules.sysinfo.phone

import java.io.File

class PhoneRooting {
    fun isRooting(): Boolean {
        return hasRootPermissionFile()
    }

    private fun isFileExist(file: String?): Boolean {
        try {
            return File(file).exists()
        } catch (e: Exception) {
        }
        return false
    }

    private fun hasRootPermissionFile(): Boolean {
        return isFileExist("/system/bin/su") ||
                isFileExist("/system/xbin/su")
    }
}