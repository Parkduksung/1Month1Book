package com.rsupport.mobile.agent.service

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.WorkerThread
import com.rsupport.commons.net.socket.compat.policy.OnReconnectListener
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.constant.AgentBasicInfo
import com.rsupport.mobile.agent.constant.ChannelInfo
import com.rsupport.mobile.agent.constant.ViewerType
import com.rsupport.mobile.agent.modules.channel.*
import com.rsupport.mobile.agent.modules.channel.CRCAgentScreenChannel.OnSendPacketListener
import com.rsupport.mobile.agent.modules.channel.screen.CodecAdapterFactory
import com.rsupport.mobile.agent.modules.channel.screen.StreamController
import com.rsupport.mobile.agent.modules.channel.screen.factory.ScreenStreamFactoryImpl
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.modules.net.*
import com.rsupport.mobile.agent.modules.net.channel.DataChannelImpl.OnMouseEventListener
import com.rsupport.mobile.agent.modules.net.protocol.MessageID
import com.rsupport.mobile.agent.modules.push.RSPushMessaging
import com.rsupport.mobile.agent.repo.config.ProxyInfo
import com.rsupport.mobile.agent.status.AgentStatus
import com.rsupport.mobile.agent.utils.AgentLogManager
import com.rsupport.mobile.agent.utils.Converter
import com.rsupport.mobile.agent.utils.SdkVersion
import com.rsupport.mobile.agent.utils.timer.DebounceTimer
import com.rsupport.mobile.agent.utils.timer.IgnoreDebounceTimer
import com.rsupport.mobile.agent.utils.timer.ThreadDebounceTimer
import com.rsupport.util.log.RLog
import org.koin.java.KoinJavaComponent.inject
import java.util.concurrent.CountDownLatch

class HxdecThread(context: Context, ip: String?, port: Int, viewerType: ViewerType?, proxyInfo: ProxyInfo? = null) : OnConfigChangeListener {
    private val agentLogManager by inject(AgentLogManager::class.java)
    private val agentStatus by inject(AgentStatus::class.java)
    private val channelInfo: ChannelInfo = ChannelInfo()
    private val screenStreamController = ScreenStreamController()
    private val context: Context = context.applicationContext
    private val channelMap = HashMap<Int, CRCChannel>()

    private var dataChannelListener: OnChannelListener? = null
    private var mouseEventListener: OnMouseEventListener? = null
    private var sendScreenPacketListener: OnSendPacketListener? = null
    private var screenChannelId = MessageID.rcpChannelScreen
    private var isReleased = false
    private val engineChecker by inject(EngineTypeCheck::class.java)
    private val rspermService by inject(RSPermService::class.java)
    private val sdkVersion by inject(SdkVersion::class.java)

    init {
        channelInfo.vhubip = ip
        channelInfo.vhubport = port
        channelInfo.viewerType = viewerType!!
        channelInfo.proxyInfo = proxyInfo
    }

    /**
     * 데이터 채널이 연결되어 있으면 true, 그렇지 않으면 false
     */
    val isAlive: Boolean
        get() {
            synchronized(this) {
                if (isReleased) return false
            }
            return channelMap[MessageID.rcpChannelData]?.isConnected() ?: false
        }

    /**
     * 화면 전송을하고 있으면 true, 그렇지 않으면 false
     */
    val isConnectedScreen: Boolean
        get() = channelMap[screenChannelId]?.isConnected() ?: false

    val channelAgentFTP: CRCAgentFTPChannel?
        get() = channelMap[MessageID.rcpChannelSFTP] as? CRCAgentFTPChannel

    fun setOnDataChannelListener(dataChannelListener: OnChannelListener?) {
        this.dataChannelListener = dataChannelListener
    }

    fun setOnMouseEventListener(mouseEventListener: OnMouseEventListener?) {
        this.mouseEventListener = mouseEventListener
    }

    fun setSendScreenPacketListener(sendPacketListener: OnSendPacketListener) {
        this.sendScreenPacketListener = sendPacketListener
    }

    private fun connectAgentDataChannel(channelInfo: ChannelInfo) {
        val connectLatch = ConnectLatch(dataOnChannelListener)
        synchronized(this) {
            if (isReleased) return
            channelMap[channelInfo.channelRequest.channelId] = CRCAgentDataChannel(context, DebounceTimerFactory(channelInfo.viewerType).create()).apply {
                setOnChannelListener(connectLatch)
                setSocketCompatFactory(DataChannelSocketCompatFactory(channelInfo, true, onDataChannelReconnectListener))
                setChannelFactory(DataChannelFactory(context, mouseEventListener!!, screenStreamController))
                startThread()
            }
        }
        connectLatch.await()
    }

    private fun sendConnectedResultMessage(isSucces: Boolean) {
        val sendByte = Converter.getBytesFromIntLE(if (isSucces) 1 else 0)
        val messaging = RSPushMessaging.getInstance()
        messaging.send(AgentBasicInfo.getAgentGuid(context) + "/channel", sendByte)
    }

    private fun connectAgentScreenChannel(channelInfo: ChannelInfo) {
        val connectLatch = ConnectLatch()
        synchronized(this) {
            if (isReleased) return
            engineChecker.checkEngineType()
            screenChannelId = channelInfo.channelRequest.channelId
            channelMap[channelInfo.channelRequest.channelId] = CRCAgentScreenChannel(
                    context,
                    ScreenStreamFactoryImpl(context, engineChecker, rspermService, sdkVersion),
                    DebounceTimerFactory(channelInfo.viewerType).create()
            ).apply {
                screenStreamController.setDelegate(this)

                setOnChannelListener(connectLatch)
                setSocketCompatFactory(VideoSocketCompatFactory(channelInfo, false, this, onScreenChannelReconnectListener))
                setSendPacketListener(sendScreenPacketListener)
                setCodecDataHandler(CodecAdapterFactory(this).create(channelInfo))
                startThread()
            }
        }
        connectLatch.await()
    }

    private fun connectAgentFTPChannel(channelInfo: ChannelInfo) {
        val connectLatch = ConnectLatch()
        synchronized(this) {
            if (isReleased) return

            channelMap[channelInfo.channelRequest.channelId] = CRCAgentFTPChannel(context, DebounceTimerFactory(channelInfo.viewerType).create()).apply {
                setOnChannelListener(connectLatch)
                setSocketCompatFactory(BasicSocketCompatFactory(channelInfo, false))
                startThread()
            }
        }
        connectLatch.await()
    }

    @Synchronized
    fun releaseAll() {
        isReleased = true
        channelMap.forEach { (_, channel) ->
            channel.release()
        }
        channelMap.clear()
    }

    @Synchronized
    override fun onConfigChanged(newConfig: Configuration) {
        if (isAlive) {
            channelMap.forEach { (_, channel) ->
                channel.onConfigChanged(newConfig)
            }
        }
    }

    @WorkerThread
    fun connectChannel(channelId: Int, port: Int, connectGuid: String) {
        connectChannel(ChannelRequest(channelId, port, connectGuid, AgentBasicInfo.getAgentGuid(context)))
    }

    private fun connectChannel(channelRequest: ChannelRequest) {
        channelInfo.channelRequest = channelRequest
        RLog.v("connectChannel > $channelInfo")
        when (channelRequest.channelId) {
            MessageID.rcpChannelData -> connectAgentDataChannel(channelInfo)
            MessageID.rcpChannelScreen, MessageID.rcpChannelHXScreen -> connectAgentScreenChannel(channelInfo)
            MessageID.rcpChannelSFTP -> connectAgentFTPChannel(channelInfo)
        }
    }

    private val onDataChannelReconnectListener = object : OnReconnectListener {
        override fun onFailure() {
            RLog.v("dataChannel onFailure")
        }

        override fun onSuccess() {
            RLog.v("dataChannel onSuccess")
        }

        override fun onReconnecting(count: Int) {
            RLog.v("dataChannel onReconnecting.$count")
        }
    }

    private val onScreenChannelReconnectListener = object : OnReconnectListener {
        override fun onFailure() {
            RLog.v("screenChannel onFailure")
        }

        override fun onSuccess() {
            RLog.v("screenChannel onSuccess")
            screenStreamController.start()
        }

        override fun onReconnecting(count: Int) {
            if (count == 1) {
                screenStreamController.stop()
            }
            RLog.v("screenChannel onReconnecting.$count")
        }
    }


    private val dataOnChannelListener = object : OnChannelListener {
        override fun onConnecting() {
            dataChannelListener?.onConnecting()
            agentStatus.setRemoting()
            agentLogManager.addAgentLog(context, context.getString(R.string.agent_log_remote_open))
        }

        override fun onConnectFail() {
            dataChannelListener?.onConnectFail()
            sendConnectedResultMessage(false)
        }

        override fun onConnected() {
            dataChannelListener?.onConnected()
            sendConnectedResultMessage(true)
        }

        override fun onDisconnected() {
            dataChannelListener?.onDisconnected()
            agentLogManager.addAgentLog(context, context.getString(R.string.agent_log_remote_close))
            if (agentStatus.get() == AgentStatus.AGENT_STATUS_REMOTING) {
                agentStatus.setLoggedIn()
            }
        }
    }

    inner class ConnectLatch(private val channelListener: OnChannelListener? = null) : CountDownLatch(1), OnChannelListener {
        override fun onConnecting() {
            channelListener?.onConnecting()
        }

        override fun onConnectFail() {
            countDown()
            channelListener?.onConnectFail()
        }

        override fun onConnected() {
            countDown()
            channelListener?.onConnected()
        }

        override fun onDisconnected() {
            countDown()
            channelListener?.onDisconnected()
        }
    }

    inner class ScreenStreamController : StreamController {
        private var delegate: StreamController? = null

        fun setDelegate(streamController: StreamController) {
            this.delegate = streamController
        }

        override fun restart() {
            delegate?.restart()
        }

        override fun start() {
            delegate?.start()
        }

        override fun stop() {
            delegate?.stop()
        }

        override fun pause() {
            delegate?.stop()
        }

        override fun resume() {
            delegate?.resume()
        }

    }

    inner class DebounceTimerFactory(private val viewerType: ViewerType) {
        fun create(): DebounceTimer = when (viewerType) {
            ViewerType.XENC -> IgnoreDebounceTimer()
            ViewerType.SCAP -> ThreadDebounceTimer()
        }
    }
}