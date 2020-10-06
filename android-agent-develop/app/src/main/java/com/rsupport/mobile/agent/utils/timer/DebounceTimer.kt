package com.rsupport.mobile.agent.utils.timer

interface DebounceTimer {
    fun update()
    fun schedule(delayMillisecond: Long, runnable: Runnable)
    fun cancel()
}