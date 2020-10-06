package com.rsupport.mobile.agent.status

import android.content.Context
import androidx.core.content.edit
import com.rsupport.mobile.agent.constant.AgentBasicInfo

class PrefStatusContainer(private val context: Context) : AgentStatus.StatusContainer {

    companion object {
        const val RV_AGENT_SHARD_PARAM_AGENT_STATUS = "RV_AGENT_SHARD_PARAM_AGENT_STATUS"
    }


    override fun get(): Short {
        return AgentBasicInfo.getPreperence(context).getInt(RV_AGENT_SHARD_PARAM_AGENT_STATUS, AgentStatus.AGENT_STATUS_NOLOGIN.toInt()).toShort()
    }

    override fun set(status: Short) {
        AgentBasicInfo.getPreperence(context).edit {
            putInt(RV_AGENT_SHARD_PARAM_AGENT_STATUS, status.toInt())
        }
    }

    override fun clear() {
        set(AgentStatus.AGENT_STATUS_NOLOGIN)
    }
}