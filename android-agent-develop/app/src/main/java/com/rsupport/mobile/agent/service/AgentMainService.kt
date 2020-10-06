package com.rsupport.mobile.agent.service

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.util.Log
import android.view.Display
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.iid.FirebaseInstanceId
import com.rsupport.mobile.agent.modules.push.IPushMessaging
import com.rsupport.mobile.agent.modules.push.RSPushMessaging
import com.rsupport.mobile.agent.modules.channel.H264OutPutData
import com.rsupport.mobile.agent.modules.engine.EngineTypeCheck
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.api.ApiService
import com.rsupport.mobile.agent.api.EncoderType
import com.rsupport.mobile.agent.modules.device.power.PowerKeyController
import com.rsupport.mobile.agent.repo.config.ConfigRepository
import com.rsupport.mobile.agent.utils.Result
import com.rsupport.mobile.agent.receiver.AgentPushReceiver
import com.rsupport.mobile.agent.modules.function.MovingExitButton
import com.rsupport.mobile.agent.modules.function.TopmostText
import com.rsupport.mobile.agent.modules.net.channel.DataChannelImpl
import com.rsupport.mobile.agent.utils.Utility
import com.rsupport.rscommon.exception.RSException
import com.rsupport.rsperm.IRSPerm
import control.Converter
import org.koin.java.KoinJavaComponent.inject
import com.rsupport.mobile.agent.constant.AgentBasicInfo
import com.rsupport.mobile.agent.service.command.AgentCommand
import com.rsupport.mobile.agent.constant.AgentNotificationBar
import com.rsupport.mobile.agent.ui.exit.ExitConfirmActivity
import com.rsupport.mobile.agent.utils.AgentLogManager
import com.rsupport.mobile.agent.constant.Global
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.constant.PreferenceConstant
import com.rsupport.mobile.agent.status.AgentStatus
import com.rsupport.util.log.RLog
import org.koin.java.KoinJavaComponent
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AgentMainService : Service() {
    companion object {
        const val AGENT_SERVICE_SHOW_TOAST_TEXT = 0
        const val AGENT_SERVICE_CONNECTED = 1
        const val AGENT_SERVICE_DISCONNECTED = 2
        const val AGENT_SERVICE_SHOW_EXIT_ACTITY = 3
        const val AGENT_SERVICE_UPDATE_DATA_PACKET_SIZE = 4
        const val AGENT_SERVICE_TOP_MOST_TEXT_EVENT = 5

        /**
         * Push 메세지 처리
         */
        const val AGENT_SERVICE_PUSH_DATA = "AGENT_PUSH_DATA"
        const val AGENT_SERVICE_PUSH_CLOSE = "AGENT_SERVICE_PUSH_CLOSE"
        const val AGENT_SERVICE_PUSH_OPEN = "AGENT_SERVICE_PUSH_OPEN"
        const val AGENT_SERVICE_PUSH_OPEN_VALUE = "AGENT_SERVICE_PUSH_OPEN_VALUE"
        const val AGENT_SERVICE_NOTI_CLOSE = "AGENT_SERVICE_NOTI_CLOSE"
        const val AGENT_SERVICE_START = "AGENT_SERVICE_START"
        const val AGENT_SERVICE_MQTT_START = "AGENT_SERVICE_MQTT_START"
        const val AGENT_SERVICE_MQTT_TYPE = "AGENT_SERVICE_MQTT_TYPE"
        const val AGENT_SERVICE_FCM_TYPE_CONNECTION_CHECK = "connectionCheck"
    }


    private var sendKbyteSize = 0.0
    private var topmostText: TopmostText? = null
    private var exitButton: MovingExitButton? = null

    private val agentCommand = AgentCommand()
    private val rspermService by inject(RSPermService::class.java)
    private val engineTypeChecker by inject(EngineTypeCheck::class.java)
    private val agentStatus by inject(AgentStatus::class.java)
    private val agentLogManager by inject(AgentLogManager::class.java)
    private val apiService by inject(ApiService::class.java)

    private val display: Display by lazy {
        (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
    }

    private val executorService: ExecutorService by lazy {
        Executors.newSingleThreadExecutor()
    }

    private lateinit var serviceHandle: Handler

    private val configRepository by inject(ConfigRepository::class.java)
    private val powerKeyController by inject(PowerKeyController::class.java)

    override fun onCreate() {
        RLog.d("AgentMainService onCreate")
        super.onCreate()
        serviceHandle = Handler(serviceCallback)
        agentCommand.setServiceHandler(serviceHandle)
        initValues()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            return START_NOT_STICKY
        }

        if (intent.hasExtra(AGENT_SERVICE_PUSH_DATA)) {
            RLog.d("onStartCommand AGENT_SERVICE_PUSH_DATA")
            val data = intent.getByteArrayExtra(AGENT_SERVICE_PUSH_DATA)
            rspermService.bindRsperm(object : OnRspermBindListener {
                override fun onResult(result: Result<IRSPerm>) {
                    executorService.execute {
                        agentCommand.saveRotation(display.rotation)
                        agentCommand.readCMDCommand(data)
                    }
                }
            })
        } else if (intent.hasExtra(AGENT_SERVICE_PUSH_OPEN)) {
            val result = intent.getIntExtra(AGENT_SERVICE_PUSH_OPEN_VALUE, 0)
            RLog.d("onStartCommand AGENT_SERVICE_PUSH_OPEN_VALUE : $result")
            if (result == 212 || result == 113) { // agent 삭제 또는 찾을 수 없을 경우.
                RLog.d("error login android :$result")
                configRepository.delete()
                if (Global.getInstance().agentThread != null) {
                    Global.getInstance().agentThread.releaseAll()
                    Global.getInstance().agentThread = null
                }
                RSPushMessaging.getInstance().clear()
                RLog.d("deleteAlldatas!!!")
            } else if (result == 0) {
                executorService.execute {
                    try {
                        val guid = AgentBasicInfo.getAgentGuid(this)
                        // AgentMainService로 이동...
                        agentLogManager.addAgentLog(Global.getInstance().appContext, String.format(Global.getInstance().appContext.getString(R.string.agent_log_agent_login_ok), AgentBasicInfo.RV_AGENT_PUSHSERVER_ADDRESS))
                        val ret = KoinJavaComponent.get(ApiService::class.java).agentSessionResult(guid, "0", AgentBasicInfo.RV_AGENT_PUSHSERVER_ADDRESS, AgentBasicInfo.RV_AGENT_PUSHSERVER_PORT)
                        RLog.d("call agentSessionResult : ${ret.isSuccess}")
                        // changeStatus
                        if (ret.isSuccess) {
                            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                                executorService.execute {
                                    apiService.registerFcmId(guid, it.token)
                                }
                            }
                        }
                    } catch (e: RSException) {
                        RLog.w(e)
                    }
                }
            }
        } else if (intent.hasExtra(AGENT_SERVICE_NOTI_CLOSE)) {
            RLog.d("onStartCommand AGENT_SERVICE_NOTI_CLOSE")
            if (Global.getInstance().agentThread != null) {
                Global.getInstance().agentThread.releaseAll()
                Global.getInstance().agentThread = null
            }
        } else if (intent.hasExtra(AGENT_SERVICE_START)) {
            RLog.d("onStartCommand AGENT_SERVICE_START")
            rspermService.bindRsperm(object : OnRspermBindListener {
                override fun onResult(result: Result<IRSPerm>) {
                    RLog.v("bindRspermResult." + result.isSuccess)
                    if (!isRemoteStatus()) {
                        powerKeyController.on()
                    }
                    startAgentLogin()
                }
            })
        } else if (intent.hasExtra(AGENT_SERVICE_MQTT_START)) {
            RLog.v("executorService.AGENT_SERVICE_MQTT_START")
            startAgentLogin()
        }
        return START_NOT_STICKY
    }

    private fun isRemoteStatus(): Boolean {
        return Global.getInstance().agentThread?.isAlive ?: false
    }

    override fun onDestroy() {
        RLog.d("onDestroy")
        powerKeyController.release()
        rspermService.unbindRsperm()
        Utility.releaseAlarm(this)
        super.onDestroy()
    }

    private val serviceCallback = Handler.Callback { msg: Message ->
        when (msg.what) {
            AGENT_SERVICE_SHOW_TOAST_TEXT -> {
                val message = msg.obj as String
                Toast.makeText(this@AgentMainService, message, Toast.LENGTH_LONG).show()
            }
            AGENT_SERVICE_CONNECTED -> {
                setForeground()
                setNoticeConnectingMessage()
                showText()
                connectStartQury()
            }
            AGENT_SERVICE_DISCONNECTED -> {
                disconnectStartQury()
                releaseNoticeConnectingMessage()
                hideText()
                stopForeGround()
            }
            AGENT_SERVICE_SHOW_EXIT_ACTITY -> startExitActivity()
            AGENT_SERVICE_UPDATE_DATA_PACKET_SIZE -> if (msg.obj is H264OutPutData) {
                setH264StringData(msg.obj as H264OutPutData)
            }
            AGENT_SERVICE_TOP_MOST_TEXT_EVENT -> if (msg.obj is DataChannelImpl.MouseEvent) {
                val mouseEvent = msg.obj as DataChannelImpl.MouseEvent
                when (mouseEvent.type) {
                    TopmostText.REMOTESTATE_DRAG, TopmostText.REMOTESTATE_LASER, TopmostText.REMOTESTATE_CLICK -> {
                        topmostText?.drawRemoteStateImage(mouseEvent.type, mouseEvent.x, mouseEvent.y)
                    }
                    TopmostText.REMOTESTATE_RELEASED -> {
                        topmostText?.onMouseUp()
                    }
                    TopmostText.REMOTESTATE_VISIBLE -> {
                        topmostText?.inVisiblTopmostView(false)
                    }
                    TopmostText.REMOTESTATE_INVISIBLE -> {
                        topmostText?.inVisiblTopmostView(true)
                    }
                }
            }
        }
        true
    }

    private fun startAgentLogin() {
        AgentLoginManager.getInstence().startMQTTPushService { sendAgentStatusByRSPush() }
    }

    /**
     * Viewer 에 agent 상태를 rspush 를 이용하여 보낸다.
     * [AgentStatus.AGENT_STATUS_NOLOGIN],
     * [AgentStatus.AGENT_STATUS_LOGIN],
     * [AgentStatus.AGENT_STATUS_LOGOUT],
     * [AgentStatus.AGENT_STATUS_REMOTING],
     * @return agent status
     */
    private fun sendAgentStatusByRSPush() {
        val sessionPacket = 30301
        val status = when (agentStatus.get()) {
            AgentStatus.AGENT_STATUS_LOGIN -> 0
            AgentStatus.AGENT_STATUS_REMOTING -> 1
            AgentStatus.AGENT_STATUS_LOGOUT -> 2
            else -> 0
        }

        val buf = ByteBuffer.allocate(12)
        buf.put(Converter.getBytesFromIntLE(4), 0, 4)
        buf.put(Converter.getBytesFromIntLE(sessionPacket), 0, 4)
        buf.put(Converter.getBytesFromIntLE(status), 0, 4)
        val sendByte = buf.array()
        AgentCommand.dec_bitcrosswise(sendByte, 4)
        val messaging = RSPushMessaging.getInstance()
        messaging.send(AgentBasicInfo.getAgentGuid(Global.getInstance().appContext) + "/mqtt-connect", sendByte)
    }

    private fun stopForeGround() {
        stopForeground(true)
        stopSelf()
    }

    private fun connectStartQury() {
        executorService.execute(connectWebQuryRunnable)
    }

    private fun setForeground() {
        startForeground(
                AgentNotificationBar.AGENT_NOTIFICATION_ID,
                NotificationCompat.Builder(this, AgentNotificationBar.AGENT_CHANNEL_ID)
                        .setContentTitle(getString(R.string.topmost_text))
                        .setContentText(getString(R.string.agent_notification_text))
                        .setSmallIcon(R.drawable.statusicon)
                        .setOngoing(true)
                        .setContentIntent(
                                PendingIntent.getBroadcast(
                                        this,
                                        0,
                                        Intent(this, AgentPushReceiver::class.java).apply {
                                            action = IPushMessaging.ACTION_PUSH_MESSAGING
                                            putExtra(IPushMessaging.EXTRA_KEY_TYPE, IPushMessaging.TYPE_SELF_DISCONNECT_REMOTE)
                                            putExtra(IPushMessaging.EXTRA_KEY_VALUE, AgentNotificationBar.AGENT_NOTIFICATION_ID)
                                        },
                                        PendingIntent.FLAG_CANCEL_CURRENT
                                )
                        ).build()
        )
    }

    private fun disconnectStartQury() {
        executorService.execute(disconnectWebQuryRunnable)
    }

    private val connectWebQuryRunnable = Runnable {
        AgentBasicInfo.loginKey?.let {
            // ASP 는 XENC 모드만 사용한다.
            apiService.notifyConnected(AgentBasicInfo.getAgentGuid(this@AgentMainService), it, EncoderType.XENC)
        }
    }

    private val disconnectWebQuryRunnable = Runnable {
        if (AgentBasicInfo.loginKey == null) {
            RLog.w("loginkey null")
            return@Runnable
        }

        val disconnectResult = apiService.notifyDisconnected(AgentBasicInfo.getAgentGuid(this@AgentMainService), AgentBasicInfo.loginKey)
        if (disconnectResult is Result.Failure) {
            RLog.e(Log.getStackTraceString(disconnectResult.throwable))
        }

    }

    private fun startExitActivity() {
        startActivity(
                Intent(this, ExitConfirmActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun showText() {
        val color = Color.RED
        sendKbyteSize = 0.0
        if (topmostText == null) {
            topmostText = TopmostText(this).apply {
                text = getString(R.string.topmost_text)
                textColor = color
                showMovingText()
            }
        }
        if (GlobalStatic.IS_HCI_BUILD || GlobalStatic.IS_SAMSUNGPRINTER_BUILD) return

        if (exitButton == null) {
            exitButton = MovingExitButton(this, serviceHandle)
        }
    }

    private fun setH264StringData(data: H264OutPutData) {
        sendKbyteSize += data.dataSize / 1024
        val sb = StringBuilder()
        sb.append("DebugMode\n")
        sb.append(String.format("%.2f", sendKbyteSize).toDouble())
        sb.append(" Kb")
        topmostText?.text = sb.toString()
    }

    private fun hideText() {
        topmostText?.hide()
        topmostText = null
        exitButton?.hideView()
        exitButton = null
    }

    private fun setNoticeConnectingMessage() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(GlobalStatic.CONNETING_MESSAGE_ID)
        val pIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 5 * 60 * 1000, 5 * 60 * 1000.toLong(), pIntent)
    }

    private fun releaseNoticeConnectingMessage() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(GlobalStatic.CONNETING_MESSAGE_ID)
        val pIntent = PendingIntent.getActivity(this, 0, intent, 0)
        alarmManager.cancel(pIntent)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        newConfig?.let { config ->
            Global.getInstance().agentThread?.let {
                it.onConfigChanged(config)
                engineTypeChecker.checkEngineType()
            }
        }
        super.onConfigurationChanged(newConfig)
    }

    private fun initValues() {
        GlobalStatic.loadAppInfo(this)
        GlobalStatic.loadResource(this)
        Global.getInstance().webConnection.setNetworkInfo()
        GlobalStatic.loadSettingURLInfo(this)
        val pref = getSharedPreferences(PreferenceConstant.RV_PREF_SETTING_INIT, Activity.MODE_PRIVATE)
        GlobalStatic.g_setinfoLanguage = GlobalStatic.getSystemLanguage(this)
    }
}