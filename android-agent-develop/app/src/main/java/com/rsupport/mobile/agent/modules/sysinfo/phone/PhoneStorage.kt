package com.rsupport.mobile.agent.modules.sysinfo.phone

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.text.format.Formatter
import com.rsupport.mobile.agent.utils.api
import com.rsupport.util.log.RLog
import java.io.File

class PhoneStorage(private val context: Context) {

    private val INFO_EMPTY = "EMPTY"

    fun getInternalStorageSize(): String? {
        var sizes: Array<String> = getStorageInfo(Environment.getExternalStorageDirectory())
        if (sizes.isEmpty()) return null

        if (sizes[1] == sizes[0] || Build.VERSION.SDK.toInt() < 14) {
            sizes = getStorageInfo(Environment.getDataDirectory())
            return sizes[1] + "(AVAILABLE)" + " / " + sizes[0] + "(TOTAL)"
        } else if (getSDStorageSize().toString() == sizes[1] + "(AVAILABLE)" + " / " + sizes[0] + "(TOTAL)") {
            sizes = getStorageInfo(Environment.getDataDirectory())
            return sizes[1] + "(AVAILABLE)" + " / " + sizes[0] + "(TOTAL)"
        }
        return sizes[1] + "(AVAILABLE)" + " / " + sizes[0] + "(TOTAL)"
    }

    fun getExternalStorageSize(): String? {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED_READ_ONLY == state || Environment.MEDIA_MOUNTED == state) {
            val externalPath = Environment.getExternalStorageDirectory()
            var sizes = getStorageInfo(externalPath)

            //시스템저장소와 분리된 내부저장소를 가지고 있는 단말의 SD카드경로
            if (api.isFileExist("/mnt/sdcard/_ExternalSD")) {
                sizes = getStorageInfo(externalPath, File("/mnt/sdcard/_ExternalSD"))
                if (sizes.isEmpty()) {
                    RLog.i("sdcard_EMPTY")
                    return "EMPTY"
                }
            } else if (api.isFileExist("/mnt/sdcard/external_sd")) {
                sizes = getStorageInfoICS(externalPath, File("/mnt/sdcard/external_sd"))
                if (sizes.isEmpty()) {
                    RLog.i("sdcard_EMPTY")
                    return INFO_EMPTY
                }
            }

            //내부와 외부 저장소의 크기가 같은경우 sd카드가 없는경우로 본다.
            if (getInternalStorageSize().toString() == sizes[1] + "(AVAILABLE)" + " / " + sizes[0] + "(TOTAL)") {
                return INFO_EMPTY
            }
            return if (sizes.isNotEmpty()) {
                sizes[1] + "(AVAILABLE)" + " / " + sizes[0] + "(TOTAL)"
            } else {
                INFO_EMPTY
            }
        }
        return INFO_EMPTY
    }

    fun getInternalStoragePercent(): Int {
        val stat = StatFs(Environment.getDataDirectory().absolutePath)
        val blockSize = stat.blockSize.toLong()
        val totalSize = stat.blockCount * blockSize
        val availableSize = stat.availableBlocks * blockSize
        var percent = ((totalSize - availableSize).toDouble() / totalSize.toDouble() * 100).toInt()
        if (percent < 1) {
            percent = 1
        }
        return percent
    }

    fun getExternalStoragePercent(): Int {
        //internalSD
        var stat = StatFs(Environment.getExternalStorageDirectory().absolutePath)
        var blockSize = stat.blockSize.toLong()
        var totalSize = stat.blockCount * blockSize
        var availableSize = stat.availableBlocks * blockSize

        //externalSD
        if (api.isFileExist("/mnt/sdcard/_ExternalSD")) {
            stat = StatFs(File("/mnt/sdcard/_ExternalSD").absolutePath)
            blockSize = stat.blockSize.toLong()
            totalSize += stat.blockCount * blockSize
            availableSize += stat.availableBlocks * blockSize
        }
        var percent = ((totalSize - availableSize).toDouble() / totalSize.toDouble() * 100).toInt()
        if (percent < 1) {
            percent = 1
        }
        return percent
    }

    private fun getSDStorageSize(): String? {
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED_READ_ONLY == state || Environment.MEDIA_MOUNTED == state) {
            val externalPath = Environment.getExternalStorageDirectory()
            var sizes = getStorageInfo(externalPath)

            //시스템저장소와 분리된 내부저장소를 가지고 있는 단말의 SD카드경로
            if (api.isFileExist("/mnt/sdcard/_ExternalSD")) {
                sizes = getStorageInfo(externalPath, File("/mnt/sdcard/_ExternalSD"))
                if (sizes.isEmpty()) {
                    return INFO_EMPTY
                }
            } else if (api.isFileExist("/mnt/sdcard/external_sd")) {
                sizes = getStorageInfoICS(externalPath, File("/mnt/sdcard/external_sd"))
                if (sizes.isEmpty()) {
                    return INFO_EMPTY
                }
            }
            return if (sizes.isNotEmpty()) {
                sizes[1] + "(AVAILABLE)" + " / " + sizes[0] + "(TOTAL)"
            } else {
                INFO_EMPTY
            }
        }
        return INFO_EMPTY
    }

    private fun getStorageInfoICS(internalSD: File?, externalSD: File?): Array<String> {
        if (internalSD != null && externalSD != null) {
            try {
                //path
                val stat = StatFs(internalSD.absolutePath)
                val blockSize = stat.blockSize.toLong()
                val totalSize = stat.blockCount * blockSize
                val availableSize = stat.availableBlocks * blockSize

                //path2
                val stat2 = StatFs(externalSD.absolutePath)
                val blockSize2 = stat2.blockSize.toLong()
                val totalSize2 = stat2.blockCount * blockSize2
                val availableSize2 = stat2.availableBlocks * blockSize2
                //sum
                if (totalSize == totalSize2 && availableSize == availableSize2) {
                    return arrayOf()
                } else {
                    return arrayOf(
                            Formatter.formatFileSize(context, totalSize2),
                            Formatter.formatFileSize(context, availableSize2)
                    )
                }
            } catch (e: java.lang.Exception) {
                RLog.e("Cannot access path: " + internalSD.absolutePath + " : " + externalSD.absolutePath + ":" + e.localizedMessage)
            }
        }
        return arrayOf()
    }


    private fun getStorageInfo(path: File?): Array<String> {
        if (path != null) {
            try {
                val stat = StatFs(path.absolutePath)
                val blockSize = stat.blockSize.toLong()
                return arrayOf(
                        Formatter.formatFileSize(context, stat.blockCount * blockSize),
                        Formatter.formatFileSize(context, stat.availableBlocks * blockSize)
                )
            } catch (e: Exception) {
                RLog.e("Cannot access path: " + path.absolutePath + ":" + e.localizedMessage)
            }
        }
        return emptyArray()
    }

    private fun getStorageInfo(internalSD: File?, externalSD: File?): Array<String> {
        if (internalSD != null && externalSD != null) {
            try {
                //path
                val stat = StatFs(internalSD.absolutePath)
                val blockSize = stat.blockSize.toLong()
                val totalSize = stat.blockCount * blockSize
                val availableSize = stat.availableBlocks * blockSize

                //path2
                val stat2 = StatFs(externalSD.absolutePath)
                val blockSize2 = stat2.blockSize.toLong()
                val totalSize2 = stat2.blockCount * blockSize2
                val availableSize2 = stat2.availableBlocks * blockSize2

                //sum
                if (totalSize == totalSize2 && availableSize == availableSize2) {
                    return arrayOf()
                } else {
                    return arrayOf(
                            Formatter.formatFileSize(context, totalSize2),
                            Formatter.formatFileSize(context, availableSize2)
                    )
                }
            } catch (e: java.lang.Exception) {
                RLog.e("Cannot access path: " + internalSD.absolutePath + " : " + externalSD.absolutePath + ":" + e.localizedMessage)
            }
        }
        return arrayOf()
    }
}