package com.rsupport.mobile.agent.api.model;

import com.rsupport.mobile.agent.constant.ComConstant;
import com.rsupport.mobile.agent.status.AgentStatus;

import java.io.Serializable;

public class AgentInfo implements Serializable {

    private static final long serialVersionUID = -3191304628757014111L;

    public static final int VIRTUAL_AGENT = 1;

    public String guid;
    public String name;
    public String osname = "";
    public int status = AgentStatus.AGENT_STATUS_NOLOGIN;
    public String remoteip;
    public String localip;
    public String extend = String.valueOf(ComConstant.RVFLAG_KEY_NONE);
    public String pflags;            //season 1 permission
    public String rvcfgs2;            //season 2 permission
    public String groupid;
    public String macaddr;
    public String logintime;
    public String regtime;
    public String userid;
    /**
     * case of nateon (0 : pc, 1 : ios, 2 : win, 3 : android)
     */
    public String devicetype;
    public int type;
    public String subnetmask = "0.0.0.0";
    public String agentversion = "0.0.0.0";
    public String bizId;

    public AgentInfo() {

    }

    @Override
    public String toString() {
        return "AgentInfo{" +
                "guid='" + guid + '\'' +
                ", name='" + name + '\'' +
                ", osname='" + osname + '\'' +
                ", status=" + status +
                ", remoteip='" + remoteip + '\'' +
                ", localip='" + localip + '\'' +
                ", extend='" + extend + '\'' +
                ", pflags='" + pflags + '\'' +
                ", rvcfgs2='" + rvcfgs2 + '\'' +
                ", groupid='" + groupid + '\'' +
                ", macaddr='" + macaddr + '\'' +
                ", logintime='" + logintime + '\'' +
                ", regtime='" + regtime + '\'' +
                ", userid='" + userid + '\'' +
                ", devicetype='" + devicetype + '\'' +
                ", type=" + type +
                ", subnetmask='" + subnetmask + '\'' +
                ", agentversion='" + agentversion + '\'' +
                ", bizId='" + bizId + '\'' +
                '}';
    }
}
