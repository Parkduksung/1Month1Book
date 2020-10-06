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
 * FileName: PreferenceConstant.java
 * Author  : "kyeom@rsupport.com"
 * Date    : 2013. 1. 8.
 * Purpose : Preference constants defined
 *
 * [History]
 *
 * 2013. 1. 8. -Initialize
 *
 */
package com.rsupport.mobile.agent.constant;

/**
 * @author "kyeom@rsupport.com"
 */
public class PreferenceConstant {

    /**
     * Preference Name (Title)
     **/
    public static final String RV_PREF_LOGIN = "pref.login";
    public static final String RV_PREF_LOGIN_INFO = "login.agentinfo";
    public static final String RV_PREF_SETTING_INIT = "rssetting.init";
    public static final String RV_PREF_START = "rsflag.isstart";
    public static final String RV_PREF_START_APP = "rsflag.isstartapp";
    public static final String RV_PREF_NOTICE = "notice";
    public static final String RV_PREF_NOTIFY_FLAG = "rsflag.notify";
    public static final String RV_PREF_GUIDE_VIEW = "guide.view";
    public static final String RV_PREF_BITMAP_RATIO = "bitmap.ratio";
    public static final String RV_PREF_VPRO_AMT = "vpro.amt";
    public static final String RV_PREF_WEB_LOGIC = "pref.weblogin";
    public static final String RV_PREF_RESOLUTION = "pref.resolution.";    // GUID값을 뒤어 덧붙여서 key 로 사용

    /**
     * Preference, Bundle item's name
     **/
    public static final String RV_TRUE = "true";
    public static final String RV_FALSE = "false";
    public static final String RV_AGENTNAME = "agentname";
    public static final String RV_GROUPID = "groupid";
    public static final String RV_ID = "id";
    public static final String RV_PWD = "pwd";

    /**
     * Setting Info item
     **/
    public static final String RV_SERVER_TYPE = "producttype";
    public static final String RV_SERVER_IP_PERSONAL = "serverip_personal";
    public static final String RV_SERVER_IP_CORP = "serverip_corp";
    public static final String RV_SERVER_IP_SERVER = "serverip_server";
    public static final String RV_SERVER_CORP_ID = "corpid";

    /**
     * Agent Info (& AgentList Info)
     **/
    public static final String RV_AGENT_GUID = "agentguid";

    /**
     * Resolution
     **/
    public static final String RV_RESOLUTION_WIDTH = "resolution_width";
    public static final String RV_RESOLUTION_HEIGHT = "resolution_heigth";
    public static final String RV_RESOLUTION_COLORBIT = "resolution_colorbit";

    /**
     * Common
     **/
    public static final String RV_STATE_STATED = "isstart";
}
