package com.rsupport.mobile.agent.repo.agent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.map
import com.rsupport.mobile.agent.repo.dao.AgentDao
import com.rsupport.mobile.agent.repo.dao.AgentEntity
import com.rsupport.mobile.agent.api.model.AgentInfo
import org.koin.java.KoinJavaComponent.inject
import com.rsupport.mobile.agent.constant.AgentBasicInfo
import com.rsupport.mobile.agent.status.AgentStatus

class AgentRepository {

    private val agentDao by inject(AgentDao::class.java)
    private val context by inject(Context::class.java)
    private val agentStatus by inject(AgentStatus::class.java)

    /**
     * 설정된 Agent 정보를 반환한다.
     * @return LiveData<AgentInfo> [AgentInfo.guid] 가 있으면 device 가 등록되어 있고 그렇지 않으면 등록 되지 않은 device 이다.
     */
    fun getAgentInfo(): LiveData<AgentInfo> {
        return findByGuid(AgentBasicInfo.getAgentGuid(context))
    }

    /**
     * guid 에 해당하는 정보가 있는지 확인한다.
     * @return LiveData<AgentInfo> [AgentInfo.guid] 가 있으면 device 가 등록되어 있고 그렇지 않으면 등록 되지 않은 device 이다.
     */
    fun findByGuid(guid: String): LiveData<AgentInfo> {
        return agentDao.loadAgentEntity(guid).distinctUntilChanged().map { agentEntity ->
            return@map agentEntity?.let {
                AgentInfo().apply {
                    this.guid = it.guid
                    this.name = it.name
                    this.status = agentStatus.get().toInt()
                    this.macaddr = it.macaddr
                    this.localip = it.localip
                    this.devicetype = it.devicetype
                    this.extend = it.extend
                    this.osname = it.osname
                    this.bizId = it.bizId
                }
            } ?: AgentInfo()
        }
    }

    /**
     * Agent 정보를 추가한다.
     * 이미 있는 guid 면 update 한다.
     * @param agentInfo agent 정보
     */
    fun insert(agentInfo: AgentInfo) {
        AgentBasicInfo.setAgentGuid(context, agentInfo.guid)
        agentDao.insert(AgentEntity.from(agentInfo))
    }

    /**
     * guid 기반으로 등록된 agent를 제거한다.
     * @param guid [AgentInfo.guid]
     */
    fun delete(guid: String) {
        agentDao.delete(AgentEntity(guid))
    }

    /**
     * Agent 정보를 update 한다.
     * @param agentInfo agent 정보
     */
    fun update(agentInfo: AgentInfo) {
        agentDao.update(AgentEntity.from(agentInfo))
    }

    /**
     * agentTable 을 삭제한다.
     */
    fun clearAll() {
        agentDao.deleteAll()
    }
}