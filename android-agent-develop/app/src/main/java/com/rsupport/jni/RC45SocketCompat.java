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
 * FileName: RC45Stream.java
 * Author  : "kyeom@rsupport.com"
 * Date    : 2013. 7. 28.
 * Purpose : RC45를 이용하여 다른 Process 와 통신할 수 있도록 함.
 *
 * [History]
 *
 * 2013. 7. 28. -Initialize
 *
 */
package com.rsupport.jni;

import android.content.Context;

import com.rsupport.commons.net.socket.DataStream;
import com.rsupport.commons.net.socket.SocketCompat;
import com.rsupport.mobile.agent.modules.net.rc45.RCNet45;
import com.rsupport.mobile.agent.modules.net.rc45.RCNet45.NETWORKINFO_IN;
import com.rsupport.mobile.agent.modules.net.rc45.RCNet45.SERVERLOGININFO_IN;
import com.rsupport.mobile.agent.repo.config.ConfigRepository;
import com.rsupport.mobile.agent.repo.config.ProxyInfo;

import org.jetbrains.annotations.NotNull;
import org.koin.java.KoinJavaComponent;

import java.io.IOException;
import java.util.ArrayList;

import kotlin.Lazy;

import com.rsupport.mobile.agent.utils.CNetStatus;
import com.rsupport.mobile.agent.constant.Global;
import com.rsupport.util.log.RLog;

public class RC45SocketCompat implements SocketCompat, DataStream {
    private RCNet45 rcNet45;

    /* RC45 Parameter start */
    private int productType;
    private boolean isGatewayMode;

    private String uuID;
    private String userID;
    private String companyID;
    private String vhubAddr;
    private int vhubPort;
    private ArrayList<String[]> vhubinfoArr;
    private int tunnelID;

    private String viewerIP;
    private int viewerPort;
    /* RC45 Parameter end */

    private byte[] jniWriteBuffer = null;
    private byte[] writeBuffer = null;
    private byte[] readBuffer = null;

    private boolean isDisconnect = false;

    public static final int REMOTECALL4_VIEWER = 1001;
    public static final int REMOTECALL4_ACTIVEX = 1002;

    public static final int BUFFER_SIZE = 128 * 1024;
    public static final int BUFFER_SCREEN_SIZE = 1024 * 1024;
    public static final int BUFFER_SOUND_SIZE = 10 * 1024;
    public static final int BUFFER_FTP_SIZE = 1024 * 1024;

    private boolean isReqDisConnect = false;

    private static final int COMMTYPE_CHOICE = 1;

    private boolean isRunningMakeSession = false;
    private boolean isEncKey;

    private Lazy<ConfigRepository> configRepositoryLazy = KoinJavaComponent.inject(ConfigRepository.class);


    private int tryNum = 0;

    public RC45SocketCompat(Context context, int bufSize, int productType, int channelID, String uID, String address, int port, ArrayList<String[]> vhubinfoArr) {
        boolean ret = true;
        ret &= initParam(productType, channelID, uID, address, port, vhubinfoArr);
        ret &= loadLib(context);
        ret &= createBuffer(bufSize);

        if (!ret) {
            throw new RuntimeException();
        }
    }

    @Override
    public void enableEncrypt() {
        isEncKey = true;
    }

    @Override
    public boolean connect() {
        synchronized (this) {
            try {
                RLog.d("sleepConnect 500");
                Thread.sleep(500);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            SERVERLOGININFO_IN serverLoginInfo = rcNet45.createSERVERLOGININFO();

            serverLoginInfo.product = productType;
            serverLoginInfo.instantID = uuID;
            serverLoginInfo.userID = userID;
            serverLoginInfo.companyID = companyID;
            //			serverLoginInfo.commType    = SERVERLOGININFO_IN.COMM_REAL_SSL;
            serverLoginInfo.commType = COMMTYPE_CHOICE;
            serverLoginInfo.mainSession = tunnelID;

            //Case of Gateway
            if (isGatewayMode) {
                serverLoginInfo.vhubAddr = vhubAddr;
                serverLoginInfo.vhubPort = vhubPort;
            }

            NETWORKINFO_IN networkInfo = rcNet45.createNETWORKINFO();

            if (configRepositoryLazy.getValue().isProxyUse()) {
                ProxyInfo proxyInfo = configRepositoryLazy.getValue().getProxyInfo();
                networkInfo.proxyIP = proxyInfo.getAddress();
                networkInfo.porxyPort = Integer.parseInt(proxyInfo.getPort());
                networkInfo.proxyID = proxyInfo.getId();
                networkInfo.proxyPass = proxyInfo.getPwd();
            }

            //Case of P2P
            if (!isGatewayMode) {
                //Client
                if (productType == SERVERLOGININFO_IN.REMOTECALL4_ACTIVEX) {
                    networkInfo.viewerIP = viewerIP;
                }
                //Viewer, Client
                networkInfo.viewerPort = viewerPort;
            }

            boolean ret = rcNet45.createRSNetWithRetry(tunnelID);
            if (!ret) {
                RLog.e("CreateChannel fail : " + tunnelID);
                return false;
            }
            RLog.e("CreateRSNet Success : " + tunnelID);

            if (isEncKey) rcNet45.setEncKey(tunnelID);
            RLog.e("setEncKey : true : " + tunnelID);

            tryNum = 0;
            ret = rcNet45.makeSessionEx(serverLoginInfo, networkInfo);

            isRunningMakeSession = ret;

            if (!ret) {
                RLog.e("MakeSession fail : " + tunnelID);
                isRunningMakeSession = false;
                return false;
            }
            RLog.e("MakeSession Success : " + tunnelID);

            try {

                while (isRunningMakeSession) {
                    int status = rcNet45.getTunnelStatus(tunnelID);

                    RLog.e("Tunnel Status : " + status + ", ID : " + tunnelID);

                    if (this.isReqDisConnect) {
                        isRunningMakeSession = false;
                        return false;
                    }

                    if (status == RCNet45.RSNET_NORMAL_TUNNEL) {
                        //Connect Success
                        RLog.e("Connect success : " + tunnelID);
                        break;
                    }
                    if (status == RCNet45.RSNET_SELF_END) {
                        //Connect Fail
                        RLog.e("Connect fail : " + tunnelID);
                        isRunningMakeSession = false;
                        disconnect();
                        return false;
                    }
                    if (status != RCNet45.RSNET_NORMAL_END &&
                            status != RCNet45.RSNET_ANOTHER_END &&
                            status != RCNet45.RSNET_SELF_END &&
                            status != RCNet45.RSNET_NORMAL_TUNNEL) {
                        //Connect Fail
                        RLog.e("Unknown error : " + tunnelID);
                        isRunningMakeSession = false;
                        disconnect();
                        return false;
                    }

                    sleep(100);
                }

                isRunningMakeSession = false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    @Override
    public int read(byte[] buffer, int offset, int len) {

        if (rcNet45 == null) {
            return -1;
        }
        if (offset < 0 || len <= 0) {
            throw new IllegalArgumentException();
        }

        int read = 0, index = 0;

        while (len > 0) {
            read = rcNet45.read(tunnelID, len, readBuffer);
            if (read <= 0) return read;
            System.arraycopy(readBuffer, 0, buffer, offset + index, read);
            len -= read;
            index += read;
        }

        return read;
    }

    @Override
    public boolean write(byte[] sendPacket, int offset, int len) {
        boolean ret = false;
        if (len > RC45SocketCompat.BUFFER_SIZE) {
            int count = (len / RC45SocketCompat.BUFFER_SIZE) + 1;
            byte[] tempByte = new byte[RC45SocketCompat.BUFFER_SIZE];
            int position = 0;
            for (int i = 0; i < count; i++) {
                if (i == count - 1) {
                    tempByte = new byte[len - position];

                    System.arraycopy(sendPacket, position, tempByte, 0, tempByte.length);
                    ret = writeRC45(tempByte, 0, tempByte.length);
                } else {
                    System.arraycopy(sendPacket, position, tempByte, 0, tempByte.length);
                    ret = writeRC45(tempByte, 0, tempByte.length);
                    position += tempByte.length;
                }
            }

        } else {
            ret = writeRC45(sendPacket, 0, len);
        }
        return ret;
    }

    private boolean writeRC45(byte[] sendPacket, int offset, int len) {
        if (rcNet45 == null) {
            return false;
        }
        if (offset < 0 || len <= 0) {
            throw new IllegalArgumentException();
        }
        return rcNet45.writeExact(tunnelID, sendPacket, offset, len);
    }

    @Override
    public boolean isConnected() {
        boolean ret = false;

        if (rcNet45 == null) return ret;

        int status = rcNet45.getTunnelStatus(tunnelID);

        if (status == RCNet45.RSNET_NORMAL_TUNNEL) {
            ret = true;
        }

        return ret;
    }

    private void sleep(int timeMill) {
        try {
            Thread.sleep(timeMill);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean networkCheck() {
        CNetStatus net_status = CNetStatus.getInstance();
        Context context = Global.getInstance().getAppContext();
        if (!net_status.get3GStatus(context) && !net_status.getWifiStatus(context)) {
            return false;
        } else {
            return true;
        }
    }

    private boolean initParam(int type, int channelID, String uID, String address, int port, ArrayList<String[]> vhubinfoArr) {
        productType = type;        //SERVERLOGININFO_IN.REMOTECALL4_VIEWER;
        isGatewayMode = true;
        uuID = uID;
        userID = "mobile";
        companyID = "RSupport";
        vhubAddr = address;    //gatewayAddress, "192.168.160.1", "203.236.210.240", "23.23.177.129", "61.74.65.4"
        vhubPort = port;        //gatewayPort, 443
        this.vhubinfoArr = vhubinfoArr;
        tunnelID = channelID;

        return true;
    }

    private boolean loadLib(Context context) {
        rcNet45 = new RCNet45(context);
        rcNet45.load();
        return true;
    }

    private boolean createBuffer(int bufSize) {

        if (rcNet45 == null) {
            return false;
        }
        jniWriteBuffer = new byte[bufSize + RCNet45.RC45_HEADER_SIZE];
        writeBuffer = new byte[bufSize];
        readBuffer = new byte[bufSize];

        rcNet45.setWriteBuffer(jniWriteBuffer);

        return true;
    }

    @Override
    public void disconnect() {
        isReqDisConnect = true;
        if (rcNet45 == null) return;
        synchronized (this) {
            if (isDisconnect) {
                return;
            }
            isDisconnect = true;
            rcNet45.closeSession(tunnelID);
            rcNet45.deleteRSNet(tunnelID);
        }
        rcNet45 = null;
    }

    @NotNull
    @Override
    public DataStream getDataStream() {
        return this;
    }

    @Override
    public void close() throws IOException {
        disconnect();
    }
}
