package com.rsupport.mobile.agent.utils

import android.graphics.Point
import android.os.Build
import android.view.Surface
import android.view.WindowManager

class WindowDisplay(private val windowManager: WindowManager) : DisplaySize, NavigationBar {
    private val realSize = Point()

    override fun getWidth(): Int {
        windowManager.defaultDisplay.getRealSize(realSize)
        return realSize.x
    }

    override fun getHeight(): Int {
        windowManager.defaultDisplay.getRealSize(realSize)
        return realSize.y
    }

    override fun getNavigationDirection(): Int {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N && windowManager.defaultDisplay.rotation == Surface.ROTATION_270) {
            return NavigationBar.LEFT
        }
        return if (windowManager.defaultDisplay.rotation == Surface.ROTATION_180 || windowManager.defaultDisplay.rotation == Surface.ROTATION_270) {
            NavigationBar.RIGHT
        } else NavigationBar.BOTTOM
    }
}