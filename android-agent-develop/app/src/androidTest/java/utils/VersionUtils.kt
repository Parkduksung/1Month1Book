package utils

import com.rsupport.mobile.agent.utils.SdkVersion
import org.koin.java.KoinJavaComponent.inject

fun isKitKat(): Boolean {
    val sdkVersion by inject(SdkVersion::class.java)
    return sdkVersion.lessThanOrEqual19()
}