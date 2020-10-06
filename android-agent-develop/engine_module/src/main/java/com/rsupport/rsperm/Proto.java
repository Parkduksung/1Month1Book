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
 * FileName: Proto.java
 * Author  : "jjkim@rsupport.com"
 * Date    : 2013. 7. 25.
 * Purpose : BindSrnCaller 에서 사용하는 Protocol 집합.
 *
 * [History]
 *
 * 2013. 7. 25. -Initialize
 *
 */
package com.rsupport.rsperm;


public class Proto {

    public final static byte JNI_Write2DataChannel = 0;
    public final static byte JNI_SetNetworkState = 1;
    public final static byte JNI_AgentConnect = 2;
    public final static byte JNI_AgentShutdown = 3;
    public final static byte JNI_SetupInput = 4;
    public final static byte JNI_DumpCache = 5;
    public final static byte JNI_SetScreenState = 6;
    public final static byte JNI_SetOpArgs = 7;
    public final static byte JNI_CurNetType = 8;
    public final static byte JNI_SetEnableUpdate = 9;
    public final static byte JNI_SetScreenChangeNoty = 10;
    // 2011.03.17 - ftp channel.
    public final static byte JNI_FtpConnect = 11;
    public final static byte JNI_Write2FtpChannel = 12;
    public final static byte JNI_FtpShutdown = 13;

    // 2013.08.12 - ashmem share.
    public final static byte JNI_RuntimeExec = 20;
    public final static byte JNI_CreateAshmem = 21;
    public final static byte JNI_Screenshot = 22;
    public final static byte JNI_SetMaxLayer = 23;

    // screen -> inno diagnosis
    // rceng/rcengine_main.h
    final public static int INFO_CONNECTED = 1;                // CoNNE-CODE(0x0)-
    final public static int INFO_DISCONNECTED = 2;            // DISCO-CODE(0x0)-message
    final public static int INFO_SRNSERVICE_DESTROY = 3;
    final public static int INFO_DATACHANNEL = 4;            // for data channel data..
    final public static int INFO_QUERY_NETTYPE = 5;            // for exe type // query & answer of current network type
    final public static int INFO_HOLE_SCREEN_CHANGED = 6;    // for drawing mode screen change detection.
    final public static int INFO_FTPCHANNEL = 7;            // 2011.03.21 : ftp channel data.
    final public static int INFO_FTPCONNECTED = 8;            // ftp connect event.
    final public static int INFO_FTPDISCONNECTED = 9;        // ftp disconnect event.
    final public static int INFO_SCREENDISCONNECTED = 10;        // screen channel disconnected,

    // related reconnect
    final public static int RC45_RECONNECT_START = 41;        // state.about to goto reconnect mode.
    final public static int RC45_RECONNECT_COMPLETE = 42;    // state.reconnect success.
    final public static int RC45_RECONNECT_FAIL = 43;        // state.reconnect fail.

    // rc45/source/def.h
    final public static int ERR_RC45_BASE = (0x5000);
    final public static int ERR_RC45_SERVER_CONNECT_FAIL = (ERR_RC45_BASE + 1);  // TCP/IP connection fail.
    final public static int ERR_RC45_LOING_REJECT = (ERR_RC45_BASE + 2);  // Gateway login fail(No viewer).
    final public static int ERR_RC45_DNS_TO_IP_FAIL = (ERR_RC45_BASE + 3);  // Dns conversion fail.
    final public static int ERR_RC45_RECONNECT_THREAD_START_FAIL = (ERR_RC45_BASE + 4);  // Reconnect thread creation fail.
    final public static int ERR_RC45_LOGIN_INFO_CHECK_FAIL = (ERR_RC45_BASE + 5);  // Invalid connection info(guid,)
    final public static int ERR_RC45_SETTING_LOGIN_INFO_FAIL = (ERR_RC45_BASE + 6);  // Gateway login fail(Unknown).
    final public static int ERR_RC45_RECV_LOGIN_RESULT_FAIL = (ERR_RC45_BASE + 8);  // Gateway login fail(No response for login request).
    final public static int ERR_RC45_SEND_LOGIN_INFO_FAIL = (ERR_RC45_BASE + 7);  // Gateway login fail(Send login request fail).
    final public static int ERR_RC45_MAX = (ERR_RC45_BASE + 100);  // Gateway login fail(Send login request fail).

    final public static int ERR_AGENT_BASE = ERR_RC45_MAX;
    final public static int ERR_AGENT_GENERAL = (ERR_AGENT_BASE + 1);    // 0x5065
    final public static int ERR_AGENT_MAX = ERR_AGENT_BASE + 100;

    final public static int ERR_SENC_BASE = ERR_AGENT_MAX;
    final public static int ERR_SENC_FBINIT = ERR_SENC_BASE + 1;        // 0x50C9
    final public static int ERR_SENC_INPUT = ERR_SENC_BASE + 2;
    final public static int ERR_SENC_MAX = ERR_SENC_BASE + 100;

    final public static int ERR_UDS_BASE = ERR_SENC_MAX;
    final public static int ERR_UDS_INFO = ERR_UDS_BASE + 1;
    final public static int ERR_UDS_ERROR = ERR_UDS_BASE + 2;
    final public static int ERR_UDS_MAX = ERR_UDS_BASE + 100;

    final public static int ERR_NETWORK_BASE = ERR_UDS_MAX + 100;
    final public static int ERR_NET_DISABLED = ERR_NETWORK_BASE + 1;
    final public static int ERR_GET_CONNECTSTR = ERR_NETWORK_BASE + 2;
    final public static int ERR_NETWORK_MAX = ERR_NETWORK_BASE + 100;


    public final static int NetNull = 0;        // closed network..
    public final static int NetWifi = 11;        // Wifi
    public final static int Net3G = 3;            // 3g
    public final static int NetTransition = -1;

}