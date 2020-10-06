package com.rsupport.mobile.agent.repo.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rsupport.mobile.agent.constant.ComConstant
import com.rsupport.mobile.agent.api.model.AgentInfo
import com.rsupport.mobile.agent.status.AgentStatus

@Entity(tableName = "agentEntity")
class AgentEntity(
        @PrimaryKey
        @ColumnInfo(name = "guid")
        var guid: String,
        @ColumnInfo(name = "name")
        var name: String? = null,
        @ColumnInfo(name = "osname")
        var osname: String? = "",
        @ColumnInfo(name = "status")
        var status: Int = AgentStatus.AGENT_STATUS_NOLOGIN.toInt(),
        @ColumnInfo(name = "remoteip")
        var remoteip: String? = null,
        @ColumnInfo(name = "localip")
        var localip: String? = null,
        @ColumnInfo(name = "extend")
        var extend: String? = ComConstant.RVFLAG_KEY_NONE.toString(),
        @ColumnInfo(name = "pflags")
        var pflags: String? = null,   //season 1 permission,
        @ColumnInfo(name = "rvcfgs2")
        var rvcfgs2: String? = null, //season 2 permission
        @ColumnInfo(name = "groupid")
        var groupid: String? = null,
        @ColumnInfo(name = "macaddr")
        var macaddr: String? = null,
        @ColumnInfo(name = "logintime")
        var logintime: String? = null,
        @ColumnInfo(name = "regtime")
        var regtime: String? = null,
        @ColumnInfo(name = "userid")
        var userid: String? = null,
        /** case of nateon (0 : pc, 1 : ios, 2 : win, 3 : android)  */
        @ColumnInfo(name = "devicetype")
        var devicetype: String? = null,
        @ColumnInfo(name = "type")
        var type: Int = 0,
        @ColumnInfo(name = "subnetmask")
        var subnetmask: String? = "0.0.0.0",
        @ColumnInfo(name = "agentversion")
        var agentversion: String? = "0.0.0.0",
        @ColumnInfo(name = "bizId")
        var bizId: String? = ""
) {
    companion object {
        fun from(agentInfo: AgentInfo): AgentEntity {
            return AgentEntity(
                    guid = agentInfo.guid,
                    name = agentInfo.name,
                    osname = agentInfo.osname,
                    status = agentInfo.status,
                    remoteip = agentInfo.remoteip,
                    localip = agentInfo.localip,
                    extend = agentInfo.extend,
                    pflags = agentInfo.pflags,
                    rvcfgs2 = agentInfo.rvcfgs2,
                    groupid = agentInfo.groupid,
                    macaddr = agentInfo.macaddr,
                    logintime = agentInfo.logintime,
                    regtime = agentInfo.regtime,
                    userid = agentInfo.userid,
                    devicetype = agentInfo.devicetype,
                    type = agentInfo.type,
                    subnetmask = agentInfo.subnetmask,
                    agentversion = agentInfo.agentversion,
                    bizId = agentInfo.bizId
            )
        }
    }
}



