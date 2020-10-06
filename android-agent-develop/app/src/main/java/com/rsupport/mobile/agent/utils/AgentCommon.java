/********************************************************************************
 *       ______   _____    __    __ _____   _____   _____    ______  _______
 *      / ___  | / ____|  / /   / // __  | / ___ | / __  |  / ___  ||___  __|
 *     / /__/ / | |____  / /   / // /  | |/ /  | |/ /  | | / /__/ /    / /
 *    / ___  |  |____  |/ /   / // /__/ // /__/ / | |  | |/ ___  |    / /
 *   / /   | |   ____| || |__/ //  ____//  ____/  | |_/ // /   | |   / /
 *  /_/    |_|  |_____/ |_____//__/    /__/       |____//_/    |_|  /_/
 *
 ********************************************************************************
 *
 * Copyright (c) 2013 RSUPPORT Co., Ltd. All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property
 * of RSUPPORT Company Limited and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to RSUPPORT
 * Company Limited and its suppliers and are protected by trade secret
 * or copyright law. Dissemination of this information or reproduction
 * of this material is strictly forbidden unless prior written permission
 * is obtained from RSUPPORT Company Limited.
 *
 * FileName: AgentCommonUI.java
 * Author  : khkim@rsupport.com
 * Date    : 2014. 3. 20.
 * Purpose : AgentList / AgentInfo 공용 UI 및 기능을 위한 common UI control
 *
 * [History]
 *
 */

package com.rsupport.mobile.agent.utils;

import android.content.Context;

import com.rsupport.mobile.agent.constant.ComConstant;
import com.rsupport.mobile.agent.R;

import com.rsupport.mobile.agent.api.model.AgentInfo;
import com.rsupport.mobile.agent.api.model.GroupInfo;
import com.rsupport.mobile.agent.constant.GlobalStatic;
import com.rsupport.mobile.agent.status.AgentStatus;

public class AgentCommon {


    public static int getAgentIconResource(Context context, AgentInfo agentInfo) {
        int resourceId = 0;

        int status = agentInfo.status;

        if (!Utility.isOnline(context) && !Utility.isSoftIncoding())
            return R.drawable.logoff_img;


        if (status == AgentStatus.AGENT_STATUS_LOGIN) {
            resourceId = R.drawable.logon_img;
        } else if (status == AgentStatus.AGENT_STATUS_LOGOUT || status == AgentStatus.AGENT_STATUS_NOLOGIN) {
            resourceId = R.drawable.logoff_img;
        } else { // 원격 제어 중
            resourceId = R.drawable.logon_img;
        }

        return resourceId;
    }

    /**
     * GroupID 에 해당하는 GroupInfo 리턴
     **/
    public static GroupInfo getParentInfo(String groupid) {
        int size = GlobalStatic.g_vecGroups.size();

        GroupInfo group = null;
        for (int i = 0; i < size; i++) {
            group = (GroupInfo) GlobalStatic.g_vecGroups.elementAt(i);
            if (group.grpid.equals(groupid)) {
                break;
            }
        }
        return group;
    }


    public static String getMostParentName() {
        String ret = "";
        int size = GlobalStatic.g_vecGroups.size();

        GroupInfo group;
        for (int i = 0; i < size; i++) {
            group = (GroupInfo) GlobalStatic.g_vecGroups.elementAt(i);
            if (group.pgrpid.equals("0")) {
                ret = group.grpname;
                break;
            }
        }
        return ret;
    }

    public static String getMostParentID() {
        String ret = "";
        int size = GlobalStatic.g_vecGroups.size();

        GroupInfo group;
        for (int i = 0; i < size; i++) {
            group = (GroupInfo) GlobalStatic.g_vecGroups.elementAt(i);
            if (group.pgrpid.equals("0")) {
                ret = group.grpid;
                break;
            }
        }
        return ret;
    }

    /**
     * 기간만료, 라이선스 수 초과일 경우 true, 정상일 경우 false
     **/
    public static boolean isExpired(String expired) {
        if ("".equals(expired) || ComConstant.AGENT_OK.equals(expired)) {
            return false;
        }
        return true;
    }
}
