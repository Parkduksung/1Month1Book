package com.rsupport.mobile.agent.utils

import android.os.Build
import android.os.Build.VERSION_CODES
import com.rsupport.mobile.agent.utils.OpenClass

@OpenClass
class SdkVersion {

    /**
     * SDK 버전이 [VERSION_CODES.O] 이상인지 아닌지를 확인한다.
     * @return [VERSION_CODES.O] 이상이면 true, 그렇지 않으면 false
     */
    fun greaterThan26(): Boolean {
        return Build.VERSION.SDK_INT >= VERSION_CODES.O
    }

    /**
     * SDK 버전이 [VERSION_CODES.M] 이상인지 아닌지를 확인한다.
     * @return [VERSION_CODES.M] 이상이면 true, 그렇지 않으면 false
     */
    fun greaterThan23(): Boolean {
        return Build.VERSION.SDK_INT >= VERSION_CODES.M
    }

    /**
     * SDK 버전이 [VERSION_CODES.LOLLIPOP] 이상인지 아닌지를 확인한다.
     * @return [VERSION_CODES.LOLLIPOP] 이상이면 true, 그렇지 않으면 false
     */
    fun greaterThan21(): Boolean {
        return Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP
    }

    /**
     * Kitkat 이하 인지를 확인한다.
     * @return [VERSION_CODES.KITKAT] 이하이면 true, 그렇지 않으면 false
     */
    fun lessThanOrEqual19(): Boolean {
        return Build.VERSION.SDK_INT <= VERSION_CODES.KITKAT
    }
}