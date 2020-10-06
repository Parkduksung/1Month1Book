package utils

import android.os.Build

fun checkSamsungDevice(): Boolean {
    if ("samsung" == Build.MANUFACTURER) return true
    return false
}
