package com.rsupport.mobile.agent.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import com.rsupport.util.Screen
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class ScreenUtils : BroadcastReceiver() {

    private val context by inject(Context::class.java)

    private val _screenLiveData = MutableLiveData<Boolean>()
    val isScreenState = _screenLiveData.distinctUntilChanged()
    private val uiScope = MainScope()

    @Volatile
    private var isRegisted = false

    @Volatile
    private var isReleased = false

    init {
        uiScope.launch {
            if (isReleased) return@launch
            _screenLiveData.value = Screen.isScreenOn(context)
            val intentFilter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
            }
            context.registerReceiver(this@ScreenUtils, intentFilter)
            isRegisted = true
        }
    }

    fun release() {
        uiScope.launch {
            isReleased = true
            if (isRegisted) {
                isRegisted = false
                context.unregisterReceiver(this@ScreenUtils)
            }
        }
    }

    fun isOn(): Boolean {
        return Screen.isScreenOn(context)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.action?.let { action ->
            when (action) {
                Intent.ACTION_SCREEN_ON -> {
                    _screenLiveData.value = true
                }
                Intent.ACTION_SCREEN_OFF -> {
                    _screenLiveData.value = false
                }
            }
        }
    }
}