package com.rsupport.mobile.agent.utils.timer

import java.lang.Exception
import java.util.concurrent.atomic.AtomicBoolean

class ThreadDebounceTimer : DebounceTimer {
    private var isThreadRun = AtomicBoolean(false)
    private var packetSendTime: Long = 0
    private var NOP_INTERVAL: Long = 25000
    private var nopPacketSendThread: Thread? = null

    override fun update() {
        packetSendTime = System.currentTimeMillis()
        nopPacketSendThread?.interrupt()
    }

    override fun schedule(delayMillisecond: Long, runnable: Runnable) {
        if (isThreadRun.get()) return

        isThreadRun.set(true)
        update()
        NOP_INTERVAL = delayMillisecond
        nopPacketSendThread = object : Thread() {
            override fun run() {
                while (isThreadRun.get()) {
                    try {
                        sleep(delayMillisecond)
                        val currentTime = System.currentTimeMillis()
                        val interval = currentTime - packetSendTime
                        if (!isThreadRun.get()) break

                        if (NOP_INTERVAL <= interval) {
                            runnable.run()
                            packetSendTime = System.currentTimeMillis()
                        }
                    } catch (ie: InterruptedException) {
                    } catch (e: Exception) {
                    }
                }
            }
        }.apply { start() }
    }

    override fun cancel() {
        nopPacketSendThread?.interrupt()
        nopPacketSendThread = null
        isThreadRun.set(false)
    }
}