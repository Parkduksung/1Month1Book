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
 * FileName: RCNet45.java
 * Author  : "kyeom@rsupport.com"
 * Date    : 2012. 5. 13.
 * Purpose : Android 용 RC45 를 이용할 수 있도록 프로토콜을 구성함.
 *
 * [History]
 *
 * 2012. 5. 13. -Initialize
 *
 */
package com.rsupport.mobile.agent.modules.net.rc45;

import android.content.Context;

import com.rsupport.jni.RC45;
import com.rsupport.util.log.RLog;

import java.io.UnsupportedEncodingException;

/**
 * Android Porting Source of CRCNet42.h & CRCNet42.cpp
 * <p>
 * This Class is a library.
 * <p>
 * (Guide) [http://nforge.rsupport.com/projects/networkserver/wiki/rc45_Android_interface?action=show]
 */
public class RCNet45 {

    /*
     * return value of F_GetConnectPath
     */
    public static int RSNET_RCNT_MODE = 43001;    //Gateway Mode
    public static int RSNET_ORGRSUP_MODE = 43002;    //Viewer <-- Host
    public static int RSNET_NEWRSUP_MODE = 43003;    //Viewer --> Host

    /*
     * return value of F_GetConnectType
     */
    public static int NORMALTCP = 0;
    public static int FAKESSL = 1;
    public static int NORMALTCP_RECONNECT = 2;
    public static int FAKESSL_RECONNECT = 3;
    public static int REALSSL = 4;
    public static int REALSSL_RECONNECT = 5;

    /*
     * return value of F_GetTunnelStatus
     */
    public static final int RSNET_NORMAL_END = 40401;    //Disabled
    public static final int RSNET_ANOTHER_END = 40402;    //Not connected tunnel
    public static final int RSNET_SELF_END = 40403;    //Terminated due to errors
    public static final int RSNET_NORMAL_TUNNEL = 40404;    //Tunnel Connected

    /*
     * Function Type Value
     */
    private final byte F_DeleteRSNet = 1;
    private final byte F_CreateRSNet = 2;
    private final byte F_MakeSessionEX = 10;
    private final byte F_CloseSession = 11;
    private final byte F_WriteExact = 12;
    private final byte F_Read = 13;
    private final byte F_ReadExact = 14;
    private final byte F_IsReadable = 15;
    private final byte F_GetConnectPath = 16;
    private final byte F_GetConnectType = 17;
    private final byte F_GetTunnelStatus = 18;
    private final byte F_GetListenPort = 19;
    private final byte F_EnforceGateway = 20;
    private final byte F_SetEncKey = 21;
    private final byte F_RunReConnect = 22;
    @SuppressWarnings("unused")
    private final byte F_SetReConnectTime = 23;
    @SuppressWarnings("unused")
    private final byte F_SetNormalSocketApiMode = 24;

    /*
     * rc45 header size
     */
    public static final int RC45_HEADER_SIZE = 9;

    private Context context;
    private RC45 rc45Native;
    private byte[] writeBuffer;
//	private static RCNet45 rcNet45;
//	
//	public static synchronized RCNet45 getInstance(Context context) {
//		if (rcNet45 == null) {
//			rcNet45 = new RCNet45(context);
//			rcNet45.load();
//		}
//		return rcNet45;
//	}

    public RCNet45(Context context) {
        this.context = context;
    }

    public void setWriteBuffer(byte[] writeBuffer) {

//		if (this.writeBuffer == null) {
        this.writeBuffer = writeBuffer;
//		}
    }

    /**
     * Load native rc45.
     */
    public boolean load() {
        try {
            rc45Native = new RC45(context);
            return true;

        } catch (Exception e) {
            RLog.e(e);
        }
        return false;
    }

    /**
     * Create channel
     */
    private boolean createRSNet(final int channelNumber) {
        int offset = 0;
        offset = Conv.int2byte(F_CreateRSNet, writeBuffer, offset);
        offset = Conv.int2byte4(channelNumber, writeBuffer, offset);

        return writeToNative(writeBuffer, offset);
    }

    public boolean createRSNetWithRetry(final int channelNumber) {
        RLog.d("createRSNetWithRetry(" + channelNumber + ")");
        if (!createRSNet(channelNumber)) {
            RLog.d("START RETRY : " + channelNumber);
            closeSession(channelNumber);
            deleteRSNet(channelNumber);
            return createRSNet(channelNumber);
        }
        return true;
    }

    /**
     * Connecting to the gateway server
     */
    public boolean makeSessionEx(SERVERLOGININFO_IN serverInfo, NETWORKINFO_IN netInfo) {

        int offset = 0;
        offset = Conv.int2byte(F_MakeSessionEX, writeBuffer, offset);
        offset = serverInfo.makeLoginInfo(writeBuffer, offset);
        if (offset < 0) {
            return false;
        }
        offset = netInfo.makeNetworkInfo(writeBuffer, offset);
        if (offset < 0) {
            return false;
        }

        return writeToNative(writeBuffer, offset);
    }

    /**
     * Close channel -
     * After closeSession is called, should be called the deleteRSNet.
     */
    public boolean closeSession(int channelNumber) {

        int offset = 0;
        offset = Conv.int2byte(F_CloseSession, writeBuffer, offset);
        offset = Conv.int2byte4(channelNumber, writeBuffer, offset);

        return writeToNative(writeBuffer, offset);
    }

    /**
     * Delete channel
     */
    public boolean deleteRSNet(int channelNumber) {

        int offset = 0;
        offset = Conv.int2byte(F_DeleteRSNet, writeBuffer, offset);
        offset = Conv.int2byte4(channelNumber, writeBuffer, offset);

        return writeToNative(writeBuffer, offset);
    }

    /**
     * Write data to the other party
     */
    @Deprecated
    public boolean write(int channelNumber, byte[] buffer, int pos, int len) {

        return writeExact(channelNumber, buffer, pos, len);
    }

    /**
     * Write data to the other party
     */
    public synchronized boolean writeExact(int channelNumber, byte[] buffer, int pos, int len) {

        int offset = 0;
        offset = Conv.int2byte(F_WriteExact, writeBuffer, offset);
        offset = Conv.int2byte4(channelNumber, writeBuffer, offset);
        offset = Conv.int2byte4(len, writeBuffer, offset);
        offset = Conv.arraycopy(buffer, pos, len, writeBuffer, offset);

        return writeToNative(writeBuffer, offset);
    }

    /**
     * Read data from the other party, This is block method.
     *
     * @return (+) : success, (0 or -) : fail
     */
    public int read(int channelNumber, int readdingSize, byte[] buffer) {

        int offset = 0;
        offset = Conv.int2byte(F_Read, buffer, offset);
        offset = Conv.int2byte4(channelNumber, buffer, offset);
        offset = Conv.int2byte4(readdingSize, buffer, offset);

        return writeToNativeINT(buffer, offset);
    }

    /**
     * Read data from the other party, This is block method.
     * WaitSec-waitMillSec may not be work.
     *
     * @return 0 : success, (-) : fail
     */
    public boolean readExact(int channelNumber, int readdingSize, int waitSec, int waitMillSec, byte[] buffer) {

        int offset = 0;
        offset = Conv.int2byte(F_ReadExact, buffer, offset);
        offset = Conv.int2byte4(channelNumber, buffer, offset);
        offset = Conv.int2byte4(readdingSize, buffer, offset);
        offset = Conv.int2byte4(waitSec, buffer, offset);
        offset = Conv.int2byte4(waitMillSec, buffer, offset);

        return writeToNative(buffer, offset);
    }

    /**
     * Doesn't work on Android (returns 1 unconditional)
     *
     * @return 1 : true, 0 : Time out, (-) : false
     */
    public int IsReadable(int channelNumber, int waitSec, int waitMillSec) {

        int offset = 0;
        offset = Conv.int2byte(F_IsReadable, writeBuffer, offset);
        offset = Conv.int2byte4(channelNumber, writeBuffer, offset);
        offset = Conv.int2byte4(waitSec, writeBuffer, offset);
        offset = Conv.int2byte4(waitMillSec, writeBuffer, offset);

        return writeToNativeINT(writeBuffer, offset);
    }

    /**
     * @return RSNET_RCNT_MODE   (43001) : Gateway Mode,
     * RSNET_ORGRSUP_MODE(43002) : Viewer <-- Host,
     * RSNET_NEWRSUP_MODE(43003) : Viewer --> Host
     */
    public int getConnectPath(int channelNumber) {

        int offset = 0;
        offset = Conv.int2byte(F_GetConnectPath, writeBuffer, offset);
        offset = Conv.int2byte4(channelNumber, writeBuffer, offset);

        return writeToNativeINT(writeBuffer, offset);
    }

    /**
     * @return 0 : Normal TCP,
     * 1 : Fake SSL,
     * 2 : Normal TCP + Reconnect,
     * 3 : Fake SSL + Reconnect,
     * 4 : Real SSL (Currently not used.),
     * 5 : Real SSL + Reconnect (Currently not used.)
     */
    public int getConnectType(int channelNumber) {

        int offset = 0;
        offset = Conv.int2byte(F_GetConnectType, writeBuffer, offset);
        offset = Conv.int2byte4(channelNumber, writeBuffer, offset);

        return writeToNativeINT(writeBuffer, offset);
    }

    /**
     * @return RSNET_NORMAL_END(40401) : Disabled,
     * RSNET_ANOTHER_END(40402) : Not connected tunnel,
     * RSNET_SELF_END(40403) : Terminated due to errors,
     * RSNET_NORMAL_TUNNEL(40404) : Tunnel Connected,
     */
    public int getTunnelStatus(int channelNumber) {

        int offset = 0;
        offset = Conv.int2byte(F_GetTunnelStatus, writeBuffer, offset);
        offset = Conv.int2byte4(channelNumber, writeBuffer, offset);

        return writeToNativeINT(writeBuffer, offset);
    }

    /**
     * Check P2P Listen Port
     */
    public int getListenPort(int channelNumber) {

        int offset = 0;
        offset = Conv.int2byte(F_GetListenPort, writeBuffer, offset);
        offset = Conv.int2byte4(channelNumber, writeBuffer, offset);

        return writeToNativeINT(writeBuffer, offset);
    }

    /**
     * Only Gateway mode use (not p2p)
     */
    public void enforceGateway(int channelNumber) {

        int offset = 0;
        offset = Conv.int2byte(F_EnforceGateway, writeBuffer, offset);
        offset = Conv.int2byte4(channelNumber, writeBuffer, offset);

        writeToNative(writeBuffer, offset);
    }

    /**
     * AES encryption/decryption use
     */
    public void setEncKey(int channelNumber) {

        int offset = 0;
        offset = Conv.int2byte(F_SetEncKey, writeBuffer, offset);
        offset = Conv.int2byte4(channelNumber, writeBuffer, offset);

        writeToNative(writeBuffer, offset);
    }

    /**
     * Force reconnection
     */
    public void runReConnect(int channelNumber) {

        int offset = 0;
        offset = Conv.int2byte(F_RunReConnect, writeBuffer, offset);
        offset = Conv.int2byte4(channelNumber, writeBuffer, offset);

        writeToNative(writeBuffer, offset);
    }

    /**
     * Currently not used.
     */
    public boolean setReConnectTime() {
        return true;
    }

    /**
     * Currently not used.
     */
    public boolean setNormalSocketApiMode() {
        return true;
    }

    /**
     * return empty SERVERLOGININFO_IN instance
     */
    public SERVERLOGININFO_IN createSERVERLOGININFO() {
        return new SERVERLOGININFO_IN();
    }

    /**
     * return empty NETWORKINFO_IN instance
     */
    public NETWORKINFO_IN createNETWORKINFO() {
        return new NETWORKINFO_IN();
    }

    private boolean writeToNative(byte[] buffer, int len) {
        return rc45Native.rc45_d2n(buffer, len) == 0;
    }

    private int writeToNativeINT(byte[] buffer, int len) {
        return rc45Native.rc45_d2n(buffer, len);
    }

    public void close() {
        context = null;
        rc45Native = null;
        writeBuffer = null;
    }

    /**
     * Login information for connecting to the gateway server
     * IPV6 enable
     */
    public class SERVERLOGININFO_IN {

        public static final int REMOTECALL4_VIEWER = 1001;
        public static final int REMOTECALL4_ACTIVEX = 1002;

        //How to use (0|1|2|4)
        public static final int COMM_NORMAL = 0;
        public static final int COMM_SSL = 1;
        public static final int COMM_RECONNECT = 2;
        public static final int COMM_REAL_SSL = 4; //will be supported soon.
        public static final int COMM_GATEWAY = 6;

        public final String CHARSET = "UTF-8";

        public int product;                // 1001 or 1002
        public String instantID;                //GUID
        public String userID;
        public String companyID;
        public int group;
        public int license;
        public String vhubAddr;                //IPv4, IPv6, DNS Name
        public int vhubPort;
        public int vhubConnectTimeout;
        public String webIP;
        public String webDNS;
        public int webPort;
        public String logoutPage;
        public int webConnectTimeout;
        public int webReceiveTimeout;
        public int webReTryCount;
        public int commType;                //(0 | 1 | 2 | 4)
        public int mainSession;            //Channel Number

        int makeLoginInfo(byte[] loginBuffer, int offset) {
            try {
                offset = Conv.int2byte4(product, loginBuffer, offset);            //Essential
                offset = Conv.stringToLen_Bytes(instantID, loginBuffer, offset, CHARSET);  //Essential
                offset = Conv.stringToLen_Bytes(userID, loginBuffer, offset, CHARSET);  //Essential
                offset = Conv.stringToLen_Bytes(companyID, loginBuffer, offset, CHARSET);  //Essential
                offset = Conv.int2byte4(group, loginBuffer, offset);
                offset = Conv.int2byte4(license, loginBuffer, offset);
                offset = Conv.stringToLen_Bytes(vhubAddr, loginBuffer, offset, CHARSET);  //Essential
                offset = Conv.int2byte4(vhubPort, loginBuffer, offset);           //Essential
                offset = Conv.int2byte4(vhubConnectTimeout, loginBuffer, offset);
                offset = Conv.stringToLen_Bytes(webIP, loginBuffer, offset, CHARSET);
                offset = Conv.stringToLen_Bytes(webDNS, loginBuffer, offset, CHARSET);
                offset = Conv.int2byte4(webPort, loginBuffer, offset);
                offset = Conv.stringToLen_Bytes(logoutPage, loginBuffer, offset, CHARSET);
                offset = Conv.int2byte4(webConnectTimeout, loginBuffer, offset);
                offset = Conv.int2byte4(webReceiveTimeout, loginBuffer, offset);
                offset = Conv.int2byte4(webReTryCount, loginBuffer, offset);
                offset = Conv.int2byte4(commType, loginBuffer, offset);            //Essential
                offset = Conv.int2byte4(mainSession, loginBuffer, offset);            //Essential (Channel Number)

                return offset;

            } catch (UnsupportedEncodingException uee) {
                RLog.e(uee);
            } catch (Exception e) {
                RLog.e(e);
            }
            return -1;
        }
    }

    /**
     * Network information for connecting to the gateway server
     * IPV6 enable
     */
    public class NETWORKINFO_IN {

        public final String CHARSET = "UTF-8";

        public String activeXIP;
        public int activeXPort;
        public String viewerIP;
        public int viewerPort;
        public String proxyIP;
        public int porxyPort;
        public String proxyID;
        public String proxyPass;

        int makeNetworkInfo(byte[] networkBuffer, int offset) {
            try {
                offset = Conv.stringToLen_Bytes(activeXIP, networkBuffer, offset, CHARSET);
                offset = Conv.int2byte4(activeXPort, networkBuffer, offset);
                offset = Conv.stringToLen_Bytes(viewerIP, networkBuffer, offset, CHARSET);
                offset = Conv.int2byte4(viewerPort, networkBuffer, offset);
                offset = Conv.stringToLen_Bytes(proxyIP, networkBuffer, offset, CHARSET);
                offset = Conv.int2byte4(porxyPort, networkBuffer, offset);
                offset = Conv.stringToLen_Bytes(proxyID, networkBuffer, offset, CHARSET);
                offset = Conv.stringToLen_Bytes(proxyPass, networkBuffer, offset, CHARSET);

                return offset;

            } catch (UnsupportedEncodingException uee) {
                RLog.e(uee);
            } catch (Exception e) {
                RLog.e(e);
            }
            return -1;
        }
    }

}
