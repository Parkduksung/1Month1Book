package com.rsupport.mobile.agent.modules.push.service

import android.app.Activity
import android.app.Service
import android.content.*
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Binder
import android.os.IBinder
import android.telephony.TelephonyManager
import android.text.TextUtils
import com.rsupport.mobile.agent.R
import com.rsupport.mobile.agent.constant.AgentBasicInfo
import com.rsupport.mobile.agent.constant.Global
import com.rsupport.mobile.agent.constant.GlobalStatic
import com.rsupport.mobile.agent.constant.PreferenceConstant
import com.rsupport.mobile.agent.modules.push.IRSBinder
import com.rsupport.mobile.agent.modules.push.IRSPushService
import com.rsupport.mobile.agent.modules.push.delegate.RSPushNotificationDelegate
import com.rsupport.mobile.agent.utils.AgentLogManager
import com.rsupport.util.log.RLog
import config.EngineConfigSetting
import kotlinx.coroutines.*
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.koin.java.KoinJavaComponent.inject
import java.util.*
import kotlin.random.Random
import kotlin.random.nextLong

class RSPushService : Service(), IRSPushService, OnConnectLostListener {
    private var isPublisherReconnect: Boolean = false
    private var pushNotificationDelegate: RSPushNotificationDelegate? = null
    private val CONNECT_LOST_MIN_TIME_MILLISECOND = 5000
    private val SERVER_URI = "tcp://%s:%d"
    private val SERVER_SSL_URI = "ssl://%s:%d"
    private var serverURI = ""
    private var serverPort = 0

    private val registerTable: Hashtable<String, Publisher> = Hashtable()
    private var networkInfoString: String? = null

    private val PREF_PRIVATE_PUSH_SERVER = "pref_private_push_server"
    private val PREF_PRIVATE_PUSH_SERVER_ADDRESS = "pref_private_push_server_address"
    private val PREF_PRIVATE_PUSH_SERVER_PORT = "pref_private_push_server_port"
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val agentLogManager by inject(AgentLogManager::class.java)

    override fun onCreate() {
        RLog.v("onCreate")
        initValues()
        val intentFilter = IntentFilter()
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(netReceiver, intentFilter)
        networkInfoString = getNetworkInfo()
        agentLogManager.addAgentLog(this, getString(R.string.agent_log_push_service_start))
        restoreServerInfo()
        super.onCreate()
    }

    override fun setServerInfo(privateAddress: String?, privatePort: Int) {
        RLog.i("setServerInfo privateAddress : $privateAddress, privatePort : $privatePort")
        if (!TextUtils.isEmpty(privateAddress) && privatePort != 0) {
            clearPublisher()
            serverURI = if (AgentBasicInfo.RV_AGENT_PUSH_SSL) {
                String.format(SERVER_SSL_URI, privateAddress, privatePort)
            } else {
                String.format(SERVER_URI, privateAddress, privatePort)
            }
            serverPort = privatePort
            privateAddress?.let {
                saveServerInfo(it, serverPort)
            }
        } else {
            RLog.e("setServerInfo fail -  privateAddress : $privateAddress, privatePort : $privatePort")
        }
    }

    override fun setPublisherReconnect(isReconnect: Boolean) {
        this.isPublisherReconnect = isReconnect
    }

    private fun saveServerInfo(address: String, port: Int) {
        val pref: SharedPreferences = getSharedPreferences(PREF_PRIVATE_PUSH_SERVER, Context.MODE_PRIVATE)
        val e = pref.edit()
        e.putString(PREF_PRIVATE_PUSH_SERVER_ADDRESS, address)
        e.putInt(PREF_PRIVATE_PUSH_SERVER_PORT, port)
        e.apply()
    }

    private fun restoreServerInfo() {
        val pref: SharedPreferences = getSharedPreferences(PREF_PRIVATE_PUSH_SERVER, Context.MODE_PRIVATE)
        val ip = pref.getString(PREF_PRIVATE_PUSH_SERVER_ADDRESS, "")
        serverPort = pref.getInt(PREF_PRIVATE_PUSH_SERVER_PORT, 0)
        serverURI = if (AgentBasicInfo.RV_AGENT_PUSH_SSL) {
            String.format(SERVER_SSL_URI, ip, serverPort)
        } else {
            String.format(SERVER_URI, ip, serverPort)
        }
    }

    override fun onDestroy() {
        RLog.v("onDestroy")
        setForeground()
        unregisterReceiver(netReceiver)
        agentLogManager.addAgentLog(this, getString(R.string.agent_log_push_service_close))
        ioScope.launch {
            clearPublisher()
        }
        ioScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return RSBinder()
    }

    private fun clearPublisher() {
        RLog.i("clearPublisher")
        PushRegister(this).unRegistAll()
        synchronized(registerTable) {
            for (client in Collections.list(registerTable.elements())) {
                client.setOnConnectLostListener(null)
                client.disconnect()
                client.close()
            }
            registerTable.clear()
            setForeground()
        }
    }


    override fun register(context: Context?, topicFilter: String?) {
        RLog.i("register")
        val client: Publisher = Publisher(context, serverURI, topicFilter, MqttClient.generateClientId())
        client.setOnConnectLostListener(this)
        val identityKey = client.identityKey
        synchronized(registerTable) {
            setForeground()
            if (!registerTable.containsKey(identityKey)) {
                PushRegister(context).register(topicFilter)
                registerTable[identityKey] = client
                ioScope.launch {
                    client.run()
                }
            } else {
                RLog.w("already register $identityKey")
            }
        }
    }

    override fun unregister(context: Context?, topicFilter: String) {
        RLog.w("unregister : $topicFilter")
        synchronized(registerTable) {
            PushRegister(context).unRegist(topicFilter)
            registerTable.remove(Identity().create(context, topicFilter))?.run {
                disconnect()
                close()
            }
            if (registerTable.isEmpty) {
                stopForeground()
            }
        }
    }

    override fun setPushDelegate(pushNotificationDelegate: RSPushNotificationDelegate?) {
        this.pushNotificationDelegate = pushNotificationDelegate;
    }

    private fun setForeground() {
        pushNotificationDelegate?.setForeground(this)
    }

    private fun stopForeground() {
        pushNotificationDelegate?.stopForeground(this)
    }


    override fun pushNotification(topic: String?, message: String) {
        ioScope.launch {
            try {
                val id = MqttClient.generateClientId()
                RLog.v("send serverURI : $serverURI, id : $id")
                val client = MqttClient(serverURI, id, MemoryPersistence())
                client.setCallback(object : MqttCallback {
                    override fun connectionLost(cause: Throwable) {
                        RLog.i("send connectionLost")
                    }

                    @Throws(Exception::class)
                    override fun messageArrived(topic: String, message: MqttMessage) {
                        RLog.i("send messageArrived")
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken) {
                        RLog.i("send deliveryComplete")
                    }
                })
                val options = MqttConnectOptions()
                options.userName = "90MpdA516uPP79ldh6SNow==-R9SqEAHmNh7Sy5VQ685G7Q=="
                client.connect(options)
                val temperatureTopic = client.getTopic(topic)
                val mqttMessage = MqttMessage(message.toByteArray(EngineConfigSetting.UTF_8))
                mqttMessage.qos = 2
                temperatureTopic.publish(mqttMessage)
                client.disconnect()
            } catch (e: Exception) {
                RLog.e(e)
            }
        }
    }

    override fun connectLost(identityKey: String?) {
        RLog.i("connectLost")
        if (getNetworkInfo() == null) {
            RLog.w("not connect network")
            return
        }
        if (isPublisherReconnect) {
            ioScope.launch {
                val delayTime = Random.nextLong(LongRange(5000L, 10000))
                delay(delayTime)
                synchronized(registerTable) {
                    registerTable[identityKey]?.run()
                }
            }
        } else {
            synchronized(registerTable) {
                registerTable.remove(identityKey)?.apply {
                    close()
                }
            }
        }
    }

    private fun getIPAddress(wifiManager: WifiManager?): String? {
        if (wifiManager != null) {
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            if (ipAddress > 0) {
                return String.format("%d.%d.%d.%d", ipAddress and 0xff, ipAddress shr 8 and 0xff, ipAddress shr 16 and 0xff, ipAddress shr 24 and 0xff)
            }
        }
        return ""
    }

    private fun getNetworkInfo(): String? {
        val connectivityManager = getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            if (networkInfo.type == ConnectivityManager.TYPE_WIFI) {
                var netInfo: String? = ""
                val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
                netInfo += getIPAddress(wifiManager)
                netInfo += wifiManager.connectionInfo.ssid
                netInfo += wifiManager.connectionInfo.bssid
                return netInfo
            } else if (networkInfo.type == ConnectivityManager.TYPE_ETHERNET) {
                return ""
            } else if (networkInfo.type == ConnectivityManager.TYPE_MOBILE) {
                val telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                telephonyManager?.let {
                    val dataState = telephonyManager.dataState
                    if (dataState != TelephonyManager.DATA_DISCONNECTED) {
                        return dataState.toString()
                    }
                }
            }
        }
        return null
    }

    private val netReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                getNetworkInfo()?.let {
                    synchronized(registerTable) {
                        networkInfoString = it
                        for (client in Collections.list(registerTable.elements())) {
                            ioScope.launch { client.run() }
                        }
                    }
                } ?: run {
                    networkInfoString = null
                }
            }
        }
    }

    inner class RSBinder : Binder(), IRSBinder {
        override fun getRSPushService(): IRSPushService {
            return this@RSPushService
        }
    }

    override fun pushNotification(topic: String?, message: ByteArray?) {
        ioScope.launch {
            try {
                val id = MqttClient.generateClientId()
                RLog.v("send serverURI : $serverURI, id : $id")
                val client = MqttClient(serverURI, id, MemoryPersistence())
                client.setCallback(object : MqttCallback {
                    override fun connectionLost(cause: Throwable) {
                        RLog.i("send connectionLost")
                    }

                    @Throws(Exception::class)
                    override fun messageArrived(topic: String,
                                                message: MqttMessage) {
                        RLog.i("send messageArrived")
                    }

                    override fun deliveryComplete(token: IMqttDeliveryToken) {
                        RLog.i("send deliveryComplete")
                    }
                })
                val options = MqttConnectOptions()
                options.userName = "90MpdA516uPP79ldh6SNow==-R9SqEAHmNh7Sy5VQ685G7Q=="
                client.connect(options)
                val temperatureTopic = client.getTopic(topic)
                val mqttMessage = MqttMessage(message)
                mqttMessage.qos = 2
                temperatureTopic.publish(mqttMessage)
                client.disconnect()
            } catch (e: Exception) {
                RLog.e(e)
            }
        }
    }

    private fun initValues() {
        GlobalStatic.loadAppInfo(this)
        GlobalStatic.loadResource(this)
        Global.getInstance()
        GlobalStatic.loadSettingURLInfo(this)
        val pref: SharedPreferences = getSharedPreferences(PreferenceConstant.RV_PREF_SETTING_INIT, Activity.MODE_PRIVATE)
        GlobalStatic.g_setinfoLanguage = GlobalStatic.getSystemLanguage(this)
    }
}