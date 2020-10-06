package com.rsupport.mobile.agent.modules.push.fcm

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rsupport.mobile.agent.service.AgentMainService
import com.rsupport.mobile.agent.status.AgentStatus
import com.rsupport.util.log.RLog
import org.koin.java.KoinJavaComponent.inject

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val agentStatus: AgentStatus by inject(AgentStatus::class.java)

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        MyFirebaseInstanceIDService().onTokenRefresh(applicationContext)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        RLog.d("From: " + remoteMessage.from)

        val agentStatusValue = agentStatus.get()

        RLog.d("AgentLogin Status ==> " + agentStatusValue)
        val action = remoteMessage.data["action"]
        RLog.d("action ==> " + action)
        if (agentStatusValue == AgentStatus.AGENT_STATUS_LOGOUT || action == AgentMainService.AGENT_SERVICE_FCM_TYPE_CONNECTION_CHECK) {
            startMqttService(remoteMessage.data)
        } else {
            startMainService(remoteMessage.data)
        }
    }

    private fun startMainService(data: Map<String, String>) {
        val intent = Intent(this, AgentMainService::class.java)
        intent.putExtra(AgentMainService.AGENT_SERVICE_START, "OK")
        data.get("action")?.let {
            intent.putExtra(AgentMainService.AGENT_SERVICE_MQTT_TYPE, it)
        }
        startService(intent)
    }

    private fun startMqttService(data: Map<String, String>) {
        val intent = Intent(this, AgentMainService::class.java)
        intent.putExtra(AgentMainService.AGENT_SERVICE_MQTT_START, "OK")
        if (data.size > 0 && data["action"] != null) {
            intent.putExtra(AgentMainService.AGENT_SERVICE_MQTT_TYPE, data["action"])
        }
        startService(intent)
    }
}