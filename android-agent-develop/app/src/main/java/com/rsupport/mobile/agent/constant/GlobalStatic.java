package com.rsupport.mobile.agent.constant;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;

import com.rsupport.mobile.agent.repo.config.ConfigRepository;
import com.rsupport.mobile.agent.utils.crypto.SeedCrypto;
import com.rsupport.mobile.agent.BuildConfig;
import com.rsupport.mobile.agent.R;
import com.rsupport.mobile.agent.utils.Utility;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import config.EngineConfigSetting;

import com.rsupport.mobile.agent.api.model.AgentInfo;
import com.rsupport.mobile.agent.api.model.ConnectionInfo;
import com.rsupport.mobile.agent.api.model.GroupInfo;
import com.rsupport.mobile.agent.api.model.LoginInfo;
import com.rsupport.util.log.RLog;

import org.koin.java.KoinJavaComponent;

public class GlobalStatic {


    public static String APPVERSION_NAME = "";

    public static String g_err = "";
    public static int g_errNumber;

    public static boolean ISTOUCHVIEWGUIDE = true;
    public static boolean ISCURSORVIEWGUIDE = true;
    public static int CMD_PROC_ERR = 0;

    public static String g_deviceIP = "";
    public static String g_deviceMacAddr = "";

    public static Vector<GroupInfo> g_vecGroups = new Vector<>();
    public static GroupInfo rootGroupInfo = null;
    // public static Vector g_vecVirtuals = new Vector();
    public static Vector g_vecBackWebLogins = new Vector();
    public static AgentInfo g_agentInfo = new AgentInfo();
    public static LoginInfo g_loginInfo = new LoginInfo();
    public static String g_param = "";
    public static String g_setinfoLanguage = "";
    public static String g_agentInstallMobileNAME = "";            //표시명
    public static String g_agentInstallNAME = "";
    public static String g_agentInstallGroupID = "";
    public static String g_agentInstallGroupNAME = "";
    public static String g_agentInstallID = "";
    public static String g_agentInstallPasswd = "";
    public static String g_agentInstallPasswdRe = "";

    public static final int PRODUCT_PERSONAL = 0;
    public static final int PRODUCT_CORP = 1;
    public static final int PRODUCT_SERVER = 2;

    public final static ConnectionInfo connectionInfo = new ConnectionInfo();

    private static String macAddress = null;

    public static final int[] TEST_BITLATE = new int[]{1, 2, 4, 6, 8, 10};

    public static void loadLibrary() {
        try {
            System.loadLibrary("jzlib");
        } catch (UnsatisfiedLinkError unsatisfiedLinkError) {
        } catch (Exception e) {
            RLog.e(e);
        }
    }

    public static void loadResource(Context context) {
        loadCryptoParam(context);
    }


    /**
     * 2중 암호화
     */
    private static void loadCryptoParam(Context context) {
        //Seed encrypt key
        int[] seedEncryptKeyRes = {R.integer.i105, R.integer.i115, R.integer.i105, R.integer.i107, R.integer.i101,
                R.integer.i37, R.integer.i111, R.integer.i117, R.integer.i43, R.integer.i108,
                R.integer.i111, R.integer.i118, R.integer.i101, R.integer.i121, R.integer.i38,
                R.integer.i117, R.integer.i105, R.integer.i104, R.integer.i97, R.integer.i116,
                R.integer.i101, R.integer.i121, R.integer.i61, R.integer.i117};

        int[] seedEncryptKeyValue = new int[seedEncryptKeyRes.length];
        for (int i = 0; i < seedEncryptKeyRes.length; i++) {
            seedEncryptKeyValue[i] = context.getResources().getInteger(seedEncryptKeyRes[i]);
        }

        SeedCrypto.encryptKey = new String(seedEncryptKeyValue, 0, seedEncryptKeyValue.length);

        //AES algorithm
        int[] algorithmRes = {R.integer.i113, R.integer.i88, R.integer.i115, R.integer.i80, R.integer.i88, R.integer.i88,
                R.integer.i81, R.integer.i65, R.integer.i108, R.integer.i99, R.integer.i76, R.integer.i115,
                R.integer.i83, R.integer.i71, R.integer.i122, R.integer.i75, R.integer.i48, R.integer.i90,
                R.integer.i81, R.integer.i108, R.integer.i102, R.integer.i99, R.integer.i85, R.integer.i117,
                R.integer.i113, R.integer.i83, R.integer.i87, R.integer.i108, R.integer.i104, R.integer.i89,
                R.integer.i117, R.integer.i88, R.integer.i104, R.integer.i72, R.integer.i43, R.integer.i102,
                R.integer.i73, R.integer.i80, R.integer.i112, R.integer.i73, R.integer.i115, R.integer.i74,
                R.integer.i56, R.integer.i61};

        int[] algorithmValue = new int[algorithmRes.length];
        for (int i = 0; i < algorithmRes.length; i++) {
            algorithmValue[i] = context.getResources().getInteger(algorithmRes[i]);
        }

        String encodeSeedAlgorithm = new String(algorithmValue, 0, algorithmValue.length);
        GlobalResource.algorithm = SeedCrypto.decrypt(encodeSeedAlgorithm);

        //AES key
        int[] keyRes = {R.integer.i72, R.integer.i102, R.integer.i67, R.integer.i66, R.integer.i82, R.integer.i71,
                R.integer.i116, R.integer.i88, R.integer.i66, R.integer.i48, R.integer.i85, R.integer.i114,
                R.integer.i79, R.integer.i90, R.integer.i79, R.integer.i49, R.integer.i77, R.integer.i118,
                R.integer.i111, R.integer.i82, R.integer.i117, R.integer.i119, R.integer.i61, R.integer.i61};

        int[] keyValue = new int[keyRes.length];
        for (int i = 0; i < keyRes.length; i++) {
            keyValue[i] = context.getResources().getInteger(keyRes[i]);
        }

        String encodeSeedKeyText = new String(keyValue, 0, keyValue.length);
        GlobalResource.keyText = SeedCrypto.decrypt(encodeSeedKeyText);

        //AES ivparameter
        int[] ivRes = {R.integer.i122, R.integer.i107, R.integer.i53, R.integer.i88, R.integer.i78, R.integer.i68,
                R.integer.i67, R.integer.i73, R.integer.i49, R.integer.i78, R.integer.i102, R.integer.i82,
                R.integer.i70, R.integer.i68, R.integer.i70, R.integer.i115, R.integer.i121, R.integer.i113,
                R.integer.i85, R.integer.i81, R.integer.i70, R.integer.i119, R.integer.i61, R.integer.i61};

        int[] ivValue = new int[ivRes.length];
        for (int i = 0; i < ivRes.length; i++) {
            ivValue[i] = context.getResources().getInteger(ivRes[i]);
        }

        String encodeSeedIvParameter = new String(ivValue, 0, ivValue.length);
        GlobalResource.ivParameter = SeedCrypto.decrypt(encodeSeedIvParameter);
    }

    public static String getLocalIP() {
        String localIP = "127.0.0.1";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress.getHostAddress().toString().contains(".") &&
                                !inetAddress.getHostAddress().toString().contains(":")) {

                            localIP = inetAddress.getHostAddress().toString();
                            if (localIP != null) {
                                return localIP;
                            } else {
                                return "127.0.0.1";
                            }

                        }
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("GlobalStatic", ex.getLocalizedMessage());
        }
        return localIP;
    }

    public static String getSubnetmask(Context context) {
        String subnetmask = "";
        int index = 0;
        int networkType = 0;
        networkType = isnetworkEnabled(context);

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                index = 0;
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();

                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress.getHostAddress().toString().contains(".") && !inetAddress.getHostAddress().toString().contains(":")) {
                            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(inetAddress);

                            InterfaceAddress realAddress = networkInterface.getInterfaceAddresses().get(index);
                            RLog.d("realAddress : " + realAddress + ", " + networkInterface.getInterfaceAddresses().size() + ", " + intf.getDisplayName());
                            if (networkType == 1) {
                                RLog.d("isWifiEnabled");
                                WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                                String wifiIP = "";
                                int addr = wifiMgr.getConnectionInfo().getIpAddress();
                                if (addr != 0) {
                                    StringBuffer buf = new StringBuffer();
                                    buf.append(addr & 0xff).append('.').append((addr >>>= 8) & 0xff).append('.').append((addr >>>= 8) & 0xff).append('.').append((addr >>>= 8) & 0xff);
                                    wifiIP = buf.toString();
                                }

                                if (realAddress.toString().contains(wifiIP)) {

                                    short len = realAddress.getNetworkPrefixLength();
                                    int netmaskLen = len / 8;

                                    for (int i = 0; i < 4; i++) {
                                        if (i < netmaskLen) {
                                            subnetmask += "255";
                                        } else {
                                            subnetmask += "0";
                                        }
                                        if (i != 3) {
                                            subnetmask += ".";
                                        }
                                    }

                                    RLog.d(subnetmask);
                                    return subnetmask;
                                }
                            } else if (networkType == 2) {
                                if (intf.getDisplayName().equals("rmnet1")) { // 4g
                                    RLog.d("is4g NetMask");
                                    short len = realAddress.getNetworkPrefixLength();
                                    int netmaskLen = len / 8;
                                    for (int i = 0; i < 4; i++) {
                                        if (i < netmaskLen) {
                                            subnetmask += "255";
                                        } else {
                                            subnetmask += "0";
                                        }
                                        if (i != 3) {
                                            subnetmask += ".";
                                        }
                                    }
                                    RLog.d(subnetmask);
                                    return subnetmask;
                                }

                            } else if (networkType == 3) {
                                if (intf.getDisplayName().equals("rmnet0")) { // 4g
                                    RLog.d("is4g NetMask");
                                    short len = realAddress.getNetworkPrefixLength();
                                    int netmaskLen = len / 8;
                                    for (int i = 0; i < 4; i++) {
                                        if (i < netmaskLen) {
                                            subnetmask += "255";
                                        } else {
                                            subnetmask += "0";
                                        }
                                        if (i != 3) {
                                            subnetmask += ".";
                                        }
                                    }
                                    RLog.d(subnetmask);
                                    return subnetmask;
                                }
                            }

                        }
                        index++;
                    }
                }
            }
            RLog.d(subnetmask);
        } catch (SocketException ex) {
            RLog.e(ex.getLocalizedMessage());
        }

        return subnetmask;
    }

    public static int isnetworkEnabled(Context context) {
        int ret = 0;

        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo etherNet = manager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        try {
            if (mobile != null) {
                if (mobile.isConnected()) {
                    RLog.d("network mobile : " + mobile.isConnected() + "");
                    RLog.d("network mobile : " + mobile.getSubtype());
                    if (mobile.getSubtypeName().equals("LTE")) {
                        ret = 2;
                    } else {
                        ret = 3;
                    }
                }
            }
            if (wifi != null) {
                if (wifi.isConnected()) {
                    RLog.d("network wifi : " + wifi.isConnected() + "");
                    ret = 1;
                }
            }
            if (etherNet != null) {
                if (etherNet.isConnected()) {
                    ret = 1;
                }
            }
        } catch (NullPointerException e) {
//            e.printStackTrace();
        }
        return ret;
    }

    public static String getWifiMacAddress(Context context) {
        if (macAddress != null) return macAddress;
        try {
            String interfaceName = "wlan0";
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                    continue;
                }

                byte[] mac = intf.getHardwareAddress();
                if (mac == null) {
                    return "";
                }

                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                macAddress = buf.toString();
                return macAddress;
            }
        } catch (Exception exp) {

            exp.printStackTrace();
        }


        return macAddress == null ? "" : macAddress;
    }

    public static String getEthernetMacAddress(Context context) {
        if (macAddress != null) return macAddress;
        try {
            String interfaceName = "eth0";
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                    continue;
                }

                byte[] mac = intf.getHardwareAddress();
                if (mac == null) {
                    return "";
                }

                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                macAddress = buf.toString();
                return macAddress;
            }
        } catch (Exception exp) {

            exp.printStackTrace();
        }


        return macAddress == null ? "" : macAddress;

    }


    public static String getMacAddress(Context context) {
        if (IS_HCI_BUILD) {
            // HCI TV 단말 ethernet macaddress 사용
            return getEthernetMacAddress(context);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getWifiMacAddress(context);
        }

        if (macAddress != null) return macAddress;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (wifiManager == null) return "";

        try {

            macAddress = wifiManager.getConnectionInfo().getMacAddress();

            RLog.d("GlobalStatic getMacAddress macAddress : " + macAddress);

            ConfigRepository configRepository = KoinJavaComponent.get(ConfigRepository.class);

            if (macAddress == null &&
                    !wifiManager.isWifiEnabled() &&
                    configRepository.getProductType() != GlobalStatic.PRODUCT_PERSONAL) {

                wifiManager.setWifiEnabled(true);
                macAddress = wifiManager.getConnectionInfo().getMacAddress();
                wifiManager.setWifiEnabled(false);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return "";
        }

        return macAddress == null ? "" : macAddress;
    }


    public static void loadDeviceInfo() {
        RLog.v("BOARD : " + Build.BOARD);
        RLog.v("CPU_ABI : " + Build.CPU_ABI);
        RLog.v("DEVICE : " + Build.DEVICE);
        RLog.v("DISPLAY : " + Build.DISPLAY);
        RLog.v("FINGERPRINT : " + Build.FINGERPRINT);
        RLog.v("HOST : " + Build.HOST);
        RLog.v("ID : " + Build.ID);
        RLog.v("MANUFACTURER : " + Build.MANUFACTURER);
        RLog.v("MODEL : " + Build.MODEL);
        RLog.v("PRODUCT : " + Build.PRODUCT);
        RLog.v("TAGS : " + Build.TAGS);
        RLog.v("TYPE : " + Build.TYPE);
        RLog.v("USER : " + Build.USER);
        RLog.v("VERSION : " + Build.VERSION.RELEASE);
    }

    public static void loadAppInfo(Context context) {
        APPVERSION_NAME = Utility.getVersionName(context);
    }

    public static void loadSettingURLInfo(Context context) {
    }

    public static void loadSettingInfo(Context context) {
    }

    public static String errMessageProc(Context context) {
        switch (GlobalStatic.g_errNumber) {
            case ComConstant.NET_ERR_BIND:
            case ComConstant.NET_ERR_CONNECT:
            case ComConstant.NET_ERR_HTTPRETRY:
            case ComConstant.NET_ERR_MALFORMED:
            case ComConstant.NET_ERR_NOROUTE:
            case ComConstant.NET_ERR_PORTUNREACHABLE:
            case ComConstant.NET_ERR_PROTOCOL:
            case ComConstant.NET_ERR_TIMEOUT:
            case ComConstant.NET_ERR_UNKNOWNSERVER:
            case ComConstant.NET_ERR_UNKNOWNHOST:
            case ComConstant.NET_ERR_SOCKET:
            case ComConstant.NET_ERR_EXCEPTION:
                GlobalStatic.g_err = context.getString(R.string.msg_unableserver) + " " + context.getString(R.string.msg_checkagain);
                break;
            case ComConstant.WEB_ERR_INVALID_PARAMETER:
                GlobalStatic.g_err = context.getString(R.string.weberr_invalid_parameter);
                break;
            case ComConstant.WEB_ERR_NOT_FOUND_USERID:
            case ComConstant.WEB_ERR_AES_NOT_FOUND_USERID:
                GlobalStatic.g_err = context.getString(R.string.weberr_invalid_user_account);
                break;
            case ComConstant.WEB_ERR_NOT_FOUND_AGENTID:
                GlobalStatic.g_err = context.getString(R.string.weberr_not_found_agentid);
                break;
            case ComConstant.WEB_ERR_INVALID_USER_ACCOUNT:
            case ComConstant.WEB_ERR_AES_INVALID_USER_ACCOUNT:
                GlobalStatic.g_err = context.getString(R.string.weberr_invalid_user_account);
                break;
            case ComConstant.WEB_ERR_ALREADY_USINGSESSION:
                GlobalStatic.g_err = context.getString(R.string.weberr_aleady_usingsession);
                break;
            case ComConstant.WEB_ERR_BLOCK_MOBILELOGIN:
                GlobalStatic.g_err = context.getString(R.string.weberr_block_mobilelogin);
                break;
            case ComConstant.WEB_ERR_INVITE_EXPIRED:
                GlobalStatic.g_err = context.getString(R.string.weberr_invite_expired);
                break;
            case ComConstant.WEB_ERR_INVITE_ALREADY:
                GlobalStatic.g_err = context.getString(R.string.weberr_invite_already);
                break;
            case ComConstant.WEB_ERR_ALREADY_SAME_WORKING:
                GlobalStatic.g_err = context.getString(R.string.weberr_already_same_working);
                break;
            case ComConstant.WEB_ERR_ALREADY_DELETE_AGENTID:
                GlobalStatic.g_err = context.getString(R.string.weberr_already_delete_agentid);
                break;
            case ComConstant.WEB_ERR_AGENT_NOT_LOGIN:
                GlobalStatic.g_err = context.getString(R.string.weberr_agent_not_login);
                break;
            case ComConstant.WEB_ERR_ONLY_WEBSETUP:
                GlobalStatic.g_err = context.getString(R.string.weberr_only_websetup);
                break;
            case ComConstant.WEB_ERR_AGENT_EXPIRED:
                GlobalStatic.g_err = context.getString(R.string.weberr_agent_expired);
                break;
            case ComConstant.WEB_ERR_SQL_ERROR:
                GlobalStatic.g_err = context.getString(R.string.weberr_sql_error);
                break;
            case ComConstant.WEB_ERR_APP_VERSION:
                GlobalStatic.g_err = context.getString(R.string.weberr_app_version);
                break;
            case ComConstant.WEB_ERR_INVAILD_COMPANYID:
                GlobalStatic.g_err = context.getString(R.string.weberr_invalid_companyid);
                break;
            case ComConstant.WEB_ERR_NEED_SWITCH_MEMBER:
                GlobalStatic.g_err = context.getString(R.string.weberr_need_switch_member);
                break;
            case ComConstant.WEB_ERR_NEED_UPGRADE_MEMBER:
                GlobalStatic.g_err = context.getString(R.string.weberr_need_update_member);
                break;
            case ComConstant.WEB_ERR_WOL_NOT_FOUND_AGENT:
                GlobalStatic.g_err = context.getString(R.string.weberr_wol_not_found_agent);
                break;
            case ComConstant.WEB_ERR_INVALID_MACADDRESS:
            case ComConstant.WEB_ERR_INVALID_LOCALIP:
                GlobalStatic.g_err = context.getString(R.string.weberr_invalid_macaddress_localip);
                break;
            case ComConstant.WEB_ERR_LIC_EXPIRED:
                GlobalStatic.g_err = context.getString(R.string.weberr_lic_expired);
                break;
            case ComConstant.WEB_ERR_LIC_SERVICE_ERROR:
                GlobalStatic.g_err = context.getString(R.string.weberr_lic_service_error);
                break;
            case ComConstant.WEB_ERR_INVALID_ROLE:
                GlobalStatic.g_err = context.getString(R.string.weberr_invalid_role);
                break;
            case ComConstant.WEB_ERR_UNAUTH_MAC_ADDRESS:
                GlobalStatic.g_err = context.getString(R.string.weberr_unauth_macaddress);
                break;
            case ComConstant.WEB_ERR_UNAUTH_DEVICE:
                GlobalStatic.g_err = context.getString(R.string.weberr_unauth_device);
                break;
            case ComConstant.WEB_ERR_USER_ACCOUNT_LOCK:
                GlobalStatic.g_err = context.getString(R.string.weberr_unauth_user);
                break;
            case ComConstant.WEB_ERR_ARP_NOT_FOUND_AGENT:
                GlobalStatic.g_err = context.getString(R.string.weberr_arp_not_found_woldevice);
                break;
            case ComConstant.ERROR_ADMIN_ACCOUNT_LOCK_FOR_MINUTES:
            case ComConstant.WEB_ERR_UNAUTH_USER:

                if (GlobalStatic.connectionInfo.getAccountLock().equals("1")) {

                    if (GlobalStatic.connectionInfo.getWaitLockTime().equals("00:00")) {
                        GlobalStatic.g_err = context.getString(R.string.weberr_login_lock2);
                    } else {
                        String content = context.getString(R.string.weberr_login_lock1);
                        String lockTime = GlobalStatic.connectionInfo.getWaitLockTime();
                        String min = lockTime.substring(0, lockTime.indexOf(":"));
                        String sec = lockTime.substring(lockTime.indexOf(":") + 1, lockTime.length());

                        GlobalStatic.g_err = content.format(content, min, sec);

                        content = null;
                        lockTime = null;
                        min = null;
                        sec = null;
                    }
                } else {
                    GlobalStatic.g_err = context.getString(R.string.weberr_unauth_user);
                }
                break;
            case ComConstant.WEB_ERR_UNAUTH_PASSWORD_FAIL:

                if (GlobalStatic.connectionInfo.getAccountLock().equals("1")) {

                    if (GlobalStatic.connectionInfo.getWaitLockTime().equals("00:00")) {
                        String content = context.getString(R.string.weberr_login_wait2);
                        String loginFailCount = GlobalStatic.connectionInfo.getLoginFailCount();
                        GlobalStatic.g_err = content.format(content, loginFailCount);

                        content = null;
                        loginFailCount = null;
                    } else {
                        String content = context.getString(R.string.weberr_login_wait1);
                        String lockTime = GlobalStatic.connectionInfo.getWaitLockTime();
                        String loginFailCount = GlobalStatic.connectionInfo.getLoginFailCount();
                        String min = lockTime.substring(0, lockTime.indexOf(":"));
                        String sec = lockTime.substring(lockTime.indexOf(":") + 1, lockTime.length());

                        GlobalStatic.g_err = content.format(content, loginFailCount, min, sec);

                        content = null;
                        loginFailCount = null;
                        lockTime = null;
                        min = null;
                        sec = null;
                    }
                } else {
                    GlobalStatic.g_err = context.getString(R.string.weberr_unauth_user);
                }
                break;
            case ComConstant.WEB_ERR_ACTIVE:
                GlobalStatic.g_err = context.getString(R.string.weberr_active_fail);
                break;
            case ComConstant.WEB_ERR:
                break;
            case ComConstant.NET_ERR_WAS:
            case ComConstant.WEB_ERR_NO:
                GlobalStatic.g_err = String.format(context.getString(R.string.weberr_etc_error), String.valueOf(GlobalStatic.g_errNumber));
                break;
            case ComConstant.WEB_ERR_PASSWORD_EXPIRED:
                // 비밀번호 만료 기간이 없음.
                GlobalStatic.g_err = context.getString(R.string.weberr_password_expired);
                break;
            case ComConstant.NET_ERR_PROXYINFO_NULL:
                GlobalStatic.g_err = context.getString(R.string.proxyinfonull_msg);
                break;
            case ComConstant.NET_ERR_PROXY_VERIFY:
                GlobalStatic.g_err = context.getString(R.string.proxyverifyerr_msg);
                break;

            case ComConstant.CMD_ERR:
            case ComConstant.CMD_ERR_TOKEN:
            case ComConstant.CMD_ERR_COMSND:
            case ComConstant.CMD_ERR_DUPAGENT:
            case ComConstant.CMD_ERR_SESSION_CONNECT_FAIL:
                GlobalStatic.g_err = context.getString(R.string.cmderr_session_connect_fail);
                break;
            case ComConstant.CMD_ERR_NOAGENT:
                GlobalStatic.g_err = context.getString(R.string.cmderr_noagent);
                break;
            case ComConstant.CMD_ERR_SESSION_SEND_FAIL:
                GlobalStatic.g_err = context.getString(R.string.cmderr_session_send_fail);
                break;
            case ComConstant.CMD_ERR_SESSION_SOCKET_FAIL:
                GlobalStatic.g_err = context.getString(R.string.cmderr_session_socket_fail);
                break;
            case ComConstant.CMD_ERR_ALREADY_REMOTE_CONTROL:
                GlobalStatic.g_err = context.getString(R.string.cmderr_already_remote_control);
                break;
            case ComConstant.CMD_ERR_OPTION_FILE_CREATE_FAIL:
                GlobalStatic.g_err = context.getString(R.string.cmderr_option_file_create_fail);
                break;
            case ComConstant.CMD_ERR_PROCESS_EXCUTE_FAIL:
                GlobalStatic.g_err = context.getString(R.string.cmderr_process_execute_fail);
                break;
            case ComConstant.CMD_ERR_PROCESS_KILL_FAIL:
                GlobalStatic.g_err = context.getString(R.string.cmderr_process_kill_fail);
                break;
            case ComConstant.CMD_ERR_GET_PROCESS_LIST_FAIL:
                GlobalStatic.g_err = context.getString(R.string.cmderr_get_process_list_fail);
                break;
            case ComConstant.CMD_ERR_AGENT_RESET_CONFIG_FAIL:
                GlobalStatic.g_err = context.getString(R.string.cmderr_agent_reset_config_fail);
                break;
            case ComConstant.CMD_ERR_NOT_ALLOWED_IP_ADDRESS:
                GlobalStatic.g_err = context.getString(R.string.cmderr_not_allowed_ip_address);
                break;
            case ComConstant.CMD_ERR_GET_SCREENSHOT_FAIL:
                GlobalStatic.g_err = context.getString(R.string.cmderr_get_screenshot_fail);
                break;
            case ComConstant.CMD_ERR_GET_SYSTEM_INFO_FAIL:
                GlobalStatic.g_err = context.getString(R.string.cmderr_get_system_info_fail);
                break;
            case ComConstant.CMD_ERR_AMT_INVALID_AUTH:
                GlobalStatic.g_err = context.getString(R.string.cmderr_amt_invalid_auth);
                break;
            case ComConstant.CMD_ERR_AMT_NOT_ACCESS:
                GlobalStatic.g_err = context.getString(R.string.cmderr_amt_not_access);
                break;
            case ComConstant.CMD_ERR_AMT_INVALID_COMMAND:
                GlobalStatic.g_err = context.getString(R.string.cmderr_amt_invalid_command);
                break;
            case ComConstant.CMD_ERR_SYSTEM_REBOOT_FAIL:
                GlobalStatic.g_err = context.getString(R.string.cmderr_system_reboot_fail);
                break;
            case ComConstant.CMD_ERR_AGENTCONNECT_UNABLE:
                GlobalStatic.g_err = context.getString(R.string.cmderr_agentconnect_unable);
                break;
            case ComConstant.CMD_ERR_AMT_EXECUTE_FAIL:
            case ComConstant.CMD_ERR_CSSLEEP_EXECUTE_FAIL:
            case ComConstant.CMD_ERR_CSWAKEUP_EXECUTE_FAIL:
            case ComConstant.CMD_ERR_REMOTE_CONNECT_FAIL:
            case ComConstant.CMD_ERR_ETC:
            default:
                GlobalStatic.g_err = String.format(context.getString(R.string.weberr_etc_error), String.valueOf(GlobalStatic.g_errNumber));
        }
        return GlobalStatic.g_err;
    }


    public static boolean isAlcatelPackage(Context context) {
        if (context.getPackageName().equals("rsupport.AndroidViewer.alcatel")) {
            return true;
        }
        return false;
    }

    public static boolean isChinaPackage(Context context) {
        if (context.getPackageName().equals("rsupport.AndroidViewer.cn")) {
            return true;
        }
        return false;
    }

    public static boolean isServerPackage(Context context) {
        if (context.getPackageName().equals("rsupport.AndroidViewer.server")) {
            return true;
        }
        return false;
    }

    public static void setEditTextPosition(final EditText editText) {
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                editText.setSelection(editText.getText().length());
            }
        });
    }

    /*	kyeom@rsupport.com
     *  2012-12-11
     * 	getCountry() 를 가져오지 못하는 경우가 있어, locale 비교로 대체함.
     */
    public static String getSystemLanguage(Context context) {
        String ret = "";
//    	String country = getResources().getConfiguration().locale.getCountry();
        String locale = context.getResources().getConfiguration().locale.toString();

        if (locale.contains("ko")) {
            ret = context.getResources().getString(R.string.lang_korea);
        } else if (locale.contains("en")) {
            ret = context.getResources().getString(R.string.lang_us);
        } else if (locale.contains("ja")) {
            ret = context.getResources().getString(R.string.lang_japan);
        } else if (locale.equals("zh") || locale.contains("zh_CN")) {
            ret = context.getResources().getString(R.string.lang_china);
        } else if (locale.contains("zh_TW")) {
            ret = context.getResources().getString(R.string.lang_taiwan);
        } else if (locale.contains("es")) {
            ret = context.getResources().getString(R.string.lang_spain);
        } else if (locale.contains("pt")) {
            ret = context.getResources().getString(R.string.lang_portugal);
        }
        return ret;
    }

    public static int getBitrateTest(Context context) {
        int ret = context.getSharedPreferences("AGENT_TEST_BITRATE", context.MODE_MULTI_PROCESS).getInt("BITRATE", 1);
        RLog.d("getBitrate : " + ret);
        return ret;
    }

    public static void setBitrateTest(Context context, int bitrateNum) {
        RLog.d("setBitrate : " + bitrateNum);
        SharedPreferences mPref = context.getSharedPreferences("AGENT_TEST_BITRATE", context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt("BITRATE", bitrateNum);
        editor.commit();
    }

    public static void clearAgentInstallPageInfo() {
        GlobalStatic.g_agentInstallNAME = "";
        GlobalStatic.g_agentInstallID = "";
        GlobalStatic.g_agentInstallPasswd = "";
        GlobalStatic.g_agentInstallPasswdRe = "";
        GlobalStatic.g_agentInstallMobileNAME = "";
    }


    public static boolean isFirstAgentStart = false;
    public static final String CONNETING_MESSAGE_ID = "com.rsupport.mobile.agent.conneting_message";
    public static final String ALARM_LOGIN_CHECK_MESSAGE_ID = "com.rsupport.mobile.agent.login_check";

    public static final int androidProtocolVersion = 4;
    public static final int androidEngineVersion = 3;
    public static final boolean IS_HCI_BUILD;
    public static final boolean IS_SAMSUNGPRINTER_BUILD;
    public static final boolean IS_JAPAN_SOFT;
    public static final boolean IS_VUZIX_BUILD;
    public static final boolean IS_ZIDOO_BUILD;

    static {
        IS_SAMSUNGPRINTER_BUILD = BuildConfig.FLAVOR.equals("samsungprinter");
        IS_HCI_BUILD = BuildConfig.FLAVOR.equals("hci");
        IS_JAPAN_SOFT = BuildConfig.FLAVOR.equals("japan_soft");
        IS_VUZIX_BUILD = BuildConfig.FLAVOR.equals("vuzix");
        IS_ZIDOO_BUILD = BuildConfig.FLAVOR.equals("zidoo");
        EngineConfigSetting.setIsHCIBuild(IS_HCI_BUILD);
        EngineConfigSetting.setIsSamsungPrinter(IS_SAMSUNGPRINTER_BUILD);
        EngineConfigSetting.setIsZidoo(IS_ZIDOO_BUILD);
        EngineConfigSetting.isSoftEncoding = (IS_HCI_BUILD || IS_SAMSUNGPRINTER_BUILD || IS_JAPAN_SOFT || IS_VUZIX_BUILD);
    }


}
